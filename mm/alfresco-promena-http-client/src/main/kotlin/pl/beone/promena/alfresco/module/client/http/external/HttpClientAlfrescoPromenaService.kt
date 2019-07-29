package pl.beone.promena.alfresco.module.client.http.external

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.common.*
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.http.applicationmodel.exception.HttpException
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.netty.ByteBufFlux
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.retry.RetryExhaustedException
import reactor.util.function.Tuple2
import java.lang.System.currentTimeMillis
import java.time.Duration

class HttpClientAlfrescoPromenaService(
    private val externalCommunication: ExternalCommunication,
    private val retry: Retry,
    private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
    private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
    private val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
    private val serializationService: SerializationService,
    private val httpClient: HttpClient
) : AlfrescoPromenaService {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpClientAlfrescoPromenaService::class.java)
    }

    override fun transform(transformation: Transformation, nodeRefs: List<NodeRef>, waitMax: Duration?, retry: Retry?): List<NodeRef> {
        logger.startSync(transformation, nodeRefs, waitMax)

        return try {
            transformReactive(transformation, nodeRefs, determineRetry(retry))
                .doOnCancel {} // without it, if timeout in block(Duration) expires, reactive stream is cancelled
                .get(waitMax)
        } catch (e: IllegalStateException) {
            throw TransformationSynchronizationException(transformation, nodeRefs, waitMax)
        }
    }

    private fun <T> Mono<T>.get(waitMax: Duration?): T =
        if (waitMax != null) {
            block(waitMax)!!
        } else {
            block()!!
        }

    override fun transformAsync(transformation: Transformation, nodeRefs: List<NodeRef>, retry: Retry?): Mono<List<NodeRef>> {
        logger.startAsync(transformation, nodeRefs)

        return transformReactive(transformation, nodeRefs, determineRetry(retry)).apply {
            subscribe()
        }
    }

    private fun determineRetry(retry: Retry?): Retry =
        retry ?: this.retry

    private fun transformReactive(transformation: Transformation, nodeRefs: List<NodeRef>, retry: Retry): Mono<List<NodeRef>> {
        val startTimestamp = currentTimeMillis()

        val nodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)

        val serializedTransformationDescriptor = Mono.just(nodeRefs)
            .map(alfrescoDataDescriptorGetter::get)
            .map { dataDescriptor -> TransformationDescriptor.of(transformation, dataDescriptor) }
            .map(serializationService::serialize)

        return httpClient
            .setContentTypeHeader()
            .post()
            .setUriWithCommunicationParametersToPromena()
            .send(ByteBufFlux.fromInbound(serializedTransformationDescriptor))
            .responseSingle { response, bytes -> zipBytesWithResponse(bytes, response) }
            .map { byteArrayAndClientResponse -> handleTransformationResult(byteArrayAndClientResponse.t2, byteArrayAndClientResponse.t1) }
            .doOnNext { verifyConsistency(nodeRefs, nodesChecksum) }
            .map { (_, transformedDataDescriptor) -> alfrescoTransformedDataDescriptorSaver.save(transformation, nodeRefs, transformedDataDescriptor) }
            .doOnNext { resultNodeRefs -> logger.transformedSuccessfully(transformation, nodeRefs, resultNodeRefs, startTimestamp, currentTimeMillis()) }
            .doOnError { exception -> handleError(transformation, nodeRefs, nodesChecksum, exception) }
            .retryOnError(transformation, nodeRefs, retry)
            .onErrorMap(::unwrapRetryExhaustedException)
            .cache() // to prevent making request many times - another subscribers will receive only list of node refs
    }

    private fun HttpClient.setContentTypeHeader(): HttpClient =
        headers { it.set(HttpHeaderNames.CONTENT_TYPE, MediaTypeConstants.APPLICATION_OCTET_STREAM.mimeType) }

    private fun HttpClient.RequestSender.setUriWithCommunicationParametersToPromena(): HttpClient.RequestSender =
        uri(
            "/transform"
                    + "?id=${externalCommunication.id}"
                    + if (externalCommunication.location != null) "&location=${externalCommunication.location}" else ""
        )

    // defaultIfEmpty is necessary. In other case complete event is emitted if content is null
    private fun zipBytesWithResponse(byte: ByteBufMono, response: HttpClientResponse): Mono<Tuple2<ByteArray, HttpClientResponse>> =
        byte.asByteArray().defaultIfEmpty(ByteArray(0)).zipWith(response.toMono())

    private fun handleTransformationResult(clientResponse: HttpClientResponse, bytes: ByteArray): PerformedTransformationDescriptor =
        when (clientResponse.status()) {
            HttpResponseStatus.OK ->
                serializationService.deserialize(bytes, getClazz())
            HttpResponseStatus.INTERNAL_SERVER_ERROR ->
                throw serializationService.deserialize(bytes, clientResponse.responseHeaders().getSerializationClass())
            else ->
                throw HttpException(clientResponse.status(), bytes)
        }

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java

    @Suppress("UNCHECKED_CAST")
    private fun <T> HttpHeaders.getSerializationClass(): Class<T> =
        try {
            Class.forName(
                get(PromenaHttpHeaders.SERIALIZATION_CLASS)
                    ?: throw NoSuchElementException("Headers don't contain <${PromenaHttpHeaders.SERIALIZATION_CLASS}> entry. An unknown error occurred on Promena.")
            ) as Class<T>
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class indicated in <${PromenaHttpHeaders.SERIALIZATION_CLASS}> header isn't available", e)
        }

    private fun verifyConsistency(nodeRefs: List<NodeRef>, nodesChecksum: String) {
        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (currentNodesChecksum != nodesChecksum) {
            throw NodesInconsistencyException(nodeRefs, nodesChecksum, currentNodesChecksum)
        }
    }

    private fun handleError(transformation: Transformation, nodeRefs: List<NodeRef>, nodesChecksum: String, exception: Throwable) {
        if (exception is NodesInconsistencyException) {
            logger.skippedSavingResult(transformation, nodeRefs, exception.oldNodesChecksum, exception.currentNodesChecksum)

            throw AnotherTransformationIsInProgressException(transformation, nodeRefs, exception.oldNodesChecksum, exception.currentNodesChecksum)
        }

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            logger.couldNotTransformButChecksumsAreDifferent(transformation, nodeRefs, nodesChecksum, currentNodesChecksum, exception)

            throw AnotherTransformationIsInProgressException(
                transformation, nodeRefs, nodesChecksum, currentNodesChecksum
            )
        } else {
            logger.couldNotTransform(transformation, nodeRefs, exception)

            throw exception
        }
    }

    private fun Mono<List<NodeRef>>.retryOnError(transformation: Transformation, nodeRefs: List<NodeRef>, retry: Retry): Mono<List<NodeRef>> =
        if (retry != Retry.No) {
            retryWhen(reactor.retry.Retry.allBut<List<NodeRef>>(AnotherTransformationIsInProgressException::class.java)
                          .fixedBackoff(retry.nextAttemptDelay)
                          .retryMax(retry.maxAttempts)
                          .doOnRetry { logger.logOnRetry(transformation, nodeRefs, it.iteration(), retry.maxAttempts, retry.nextAttemptDelay) })
        } else {
            this
        }


    private fun unwrapRetryExhaustedException(exception: Throwable): Throwable =
        if (exception is RetryExhaustedException) exception.cause!! else exception
}
