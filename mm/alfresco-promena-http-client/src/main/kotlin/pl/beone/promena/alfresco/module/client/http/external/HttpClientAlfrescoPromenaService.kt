package pl.beone.promena.alfresco.module.client.http.external

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.common.*
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.http.applicationmodel.exception.HttpException
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.netty.ByteBufFlux
import reactor.netty.ByteBufMono
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.HttpClientResponse
import reactor.retry.Retry
import reactor.retry.RetryExhaustedException
import reactor.util.function.Tuple2
import java.lang.System.currentTimeMillis
import java.net.URI
import java.time.Duration

class HttpClientAlfrescoPromenaService(private val communicationLocation: URI?,
                                       private val retryOnError: Boolean,
                                       private val retryOnErrorMaxAttempts: Long,
                                       private val retryOnErrorNextAttemptsDelay: Duration,
                                       private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                       private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                       private val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                                       private val serializationService: SerializationService,
                                       private val httpClient: HttpClient)
    : AlfrescoPromenaService {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpClientAlfrescoPromenaService::class.java)

        private const val HEADER_SERIALIZATION_CLASS = "serialization-class"
    }

    override fun transform(transformerId: String,
                           nodeRefs: List<NodeRef>,
                           targetMediaType: MediaType,
                           parameters: Parameters?,
                           waitMax: Duration?): List<NodeRef> {
        val determinedParameters = determineParameters(parameters)

        logger.startSync(transformerId, nodeRefs, targetMediaType, determinedParameters, waitMax)

        return try {
            transformReactive(transformerId, nodeRefs, targetMediaType, determinedParameters)
                    .doOnCancel {} // without it, if timeout in block(Duration) expires, reactive stream is cancelled
                    .get(waitMax)
        } catch (e: IllegalStateException) {
            throw TransformationSynchronizationException(transformerId, nodeRefs, targetMediaType, determinedParameters, waitMax)
        }
    }

    private fun <T> Mono<T>.get(waitMax: Duration?): T =
            if (waitMax != null) {
                block(waitMax)!!
            } else {
                block()!!
            }

    override fun transformAsync(transformerId: String,
                                nodeRefs: List<NodeRef>,
                                targetMediaType: MediaType,
                                parameters: Parameters?): Mono<List<NodeRef>> {
        val determinedParameters = determineParameters(parameters)

        logger.startAsync(transformerId, nodeRefs, targetMediaType, determinedParameters)

        return transformReactive(transformerId, nodeRefs, targetMediaType, determinedParameters).apply {
            subscribe()
        }
    }

    private fun determineParameters(parameters: Parameters?): Parameters =
            parameters ?: MapParameters.empty()

    private fun transformReactive(transformerId: String,
                                  nodeRefs: List<NodeRef>,
                                  targetMediaType: MediaType,
                                  parameters: Parameters): Mono<List<NodeRef>> {
        val startTimestamp = currentTimeMillis()

        val nodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)

        val serializedTransformationDescriptor = Mono.just(nodeRefs)
                .map { alfrescoDataDescriptorGetter.get(nodeRefs) }
                .map { TransformationDescriptor(it, targetMediaType, parameters) }
                .map { serializationService.serialize(it) }

        return httpClient
                .headersWithContentType()
                .post()
                .transformerUriWithCommunicationLocation(transformerId)
                .send(ByteBufFlux.fromInbound(serializedTransformationDescriptor))
                .responseSingle { response, bytes -> zipBytesWithResponse(bytes, response) }
                .map { handleTransformationResult(it.t2, it.t1) }
                .doOnNext { verifyConsistency(nodeRefs, nodesChecksum) }
                .map { alfrescoTransformedDataDescriptorSaver.save(transformerId, nodeRefs, targetMediaType, it) }
                .doOnNext {
                    logger.transformedSuccessfully(transformerId, nodeRefs, targetMediaType, parameters, it, startTimestamp, currentTimeMillis())
                }
                .doOnError { handleError(transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, it) }
                .retryOnError(transformerId, parameters, nodeRefs, targetMediaType)
                .onErrorMap { unwrapRetryExhaustedException(it) }
                .cache() // to prevent making request many times - another subscribers will receive only list of node refs
    }

    private fun HttpClient.headersWithContentType(): HttpClient =
            headers { it.set(HttpHeaderNames.CONTENT_TYPE, MediaTypeConstants.APPLICATION_OCTET_STREAM.mimeType) }

    private fun HttpClient.RequestSender.transformerUriWithCommunicationLocation(transformerId: String): HttpClient.RequestSender =
            uri("/transform/$transformerId" + if (communicationLocation != null) "?location=$communicationLocation" else "")

    // defaultIfEmpty is necessary. In other case complete event is emitted if content is null
    private fun zipBytesWithResponse(byte: ByteBufMono, response: HttpClientResponse): Mono<Tuple2<ByteArray, HttpClientResponse>> =
            byte.asByteArray().defaultIfEmpty(ByteArray(0)).zipWith(response.toMono())

    private fun handleTransformationResult(clientResponse: HttpClientResponse, bytes: ByteArray): List<TransformedDataDescriptor> =
            when (clientResponse.status()) {
                HttpResponseStatus.OK                    ->
                    serializationService.deserialize(bytes, getClazz())
                HttpResponseStatus.INTERNAL_SERVER_ERROR ->
                    throw serializationService.deserialize(bytes, clientResponse.responseHeaders().getSerializationClass())
                else                                     ->
                    throw HttpException(clientResponse.status(), bytes)
            }

    private inline fun <reified T : Any> getClazz(): Class<T> =
            T::class.java

    @Suppress("UNCHECKED_CAST")
    private fun <T> HttpHeaders.getSerializationClass(): Class<T> =
            try {
                Class.forName(get(HEADER_SERIALIZATION_CLASS)
                              ?: throw NoSuchElementException("Headers don't contain <$HEADER_SERIALIZATION_CLASS> entry")) as Class<T>
            } catch (e: ClassNotFoundException) {
                throw IllegalArgumentException("Class indicated in <$HEADER_SERIALIZATION_CLASS> header isn't available", e)
            }

    private fun verifyConsistency(nodeRefs: List<NodeRef>, nodesChecksum: String) {
        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (currentNodesChecksum != nodesChecksum) {
            throw NodesInconsistencyException(nodeRefs, nodesChecksum, currentNodesChecksum)
        }
    }

    private fun handleError(transformerId: String,
                            nodeRefs: List<NodeRef>,
                            targetMediaType: MediaType,
                            parameters: Parameters,
                            nodesChecksum: String,
                            exception: Throwable) {
        if (exception is NodesInconsistencyException) {
            logger.skippedSavingResult(
                    transformerId, nodeRefs, targetMediaType, parameters, exception.oldNodesChecksum, exception.currentNodesChecksum
            )

            throw AnotherTransformationIsInProgressException(
                    transformerId, nodeRefs, targetMediaType, parameters, exception.oldNodesChecksum, exception.currentNodesChecksum
            )
        }

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            logger.couldNotTransformButChecksumsAreDifferent(
                    transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, currentNodesChecksum, exception
            )

            throw AnotherTransformationIsInProgressException(
                    transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, currentNodesChecksum
            )
        } else {
            logger.couldNotTransform(transformerId, nodeRefs, targetMediaType, parameters, exception)

            throw exception
        }
    }

    private fun Mono<List<NodeRef>>.retryOnError(transformerId: String,
                                                 parameters: Parameters,
                                                 nodeRefs: List<NodeRef>,
                                                 targetMediaType: MediaType): Mono<List<NodeRef>> =
            if (retryOnError) {
                retryWhen(Retry.allBut<List<NodeRef>>(AnotherTransformationIsInProgressException::class.java)
                                  .fixedBackoff(retryOnErrorNextAttemptsDelay)
                                  .retryMax(retryOnErrorMaxAttempts)
                                  .doOnRetry {
                                      logger.logOnRetry(it.iteration(),
                                                        retryOnErrorMaxAttempts,
                                                        transformerId,
                                                        parameters,
                                                        nodeRefs,
                                                        targetMediaType)
                                  })
            } else {
                this
            }


    private fun unwrapRetryExhaustedException(exception: Throwable): Throwable =
            if (exception is RetryExhaustedException) exception.cause!! else exception
}
