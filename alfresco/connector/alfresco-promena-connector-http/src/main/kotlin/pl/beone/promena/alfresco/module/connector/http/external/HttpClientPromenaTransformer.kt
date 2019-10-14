package pl.beone.promena.alfresco.module.connector.http.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.lib.connector.http.extension.isInstanceOfInnerPrivateStaticClass
import pl.beone.promena.lib.connector.http.extension.retryOnError
import pl.beone.promena.lib.connector.http.external.AbstractPromenaHttpTransformer
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.*
import pl.beone.promena.alfresco.module.core.extension.*
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.lang.System.currentTimeMillis
import java.time.Duration

class HttpClientPromenaTransformer(
    private val externalCommunicationParameters: CommunicationParameters,
    private val retry: Retry,
    private val nodesChecksumGenerator: NodesChecksumGenerator,
    private val dataDescriptorGetter: DataDescriptorGetter,
    private val transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
    private val authorizationService: AuthorizationService,
    serializationService: SerializationService,
    httpClient: HttpClient
) : PromenaTransformer, AbstractPromenaHttpTransformer(serializationService, httpClient) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun transform(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, waitMax: Duration?, retry: Retry?): List<NodeRef> {
        logger.startSync(transformation, nodeDescriptors, waitMax)

        return try {
            transformReactive(transformation, nodeDescriptors, determineRetry(retry))
                .doOnCancel {} // without it, if timeout in block(Duration) expires, reactive stream is cancelled
                .get(waitMax)
        } catch (e: IllegalStateException) {
            throw TransformationSynchronizationException(
                transformation,
                nodeDescriptors,
                waitMax
            )
        }
    }

    private fun <T> Mono<T>.get(waitMax: Duration?): T =
        if (waitMax != null) block(waitMax)!! else block()!!

    override fun transformAsync(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, retry: Retry?): Mono<List<NodeRef>> {
        logger.startAsync(transformation, nodeDescriptors)

        return transformReactive(transformation, nodeDescriptors, determineRetry(retry)).apply {
            subscribe()
        }
    }

    private fun determineRetry(retry: Retry?): Retry =
        retry ?: this.retry

    private fun transformReactive(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, retry: Retry): Mono<List<NodeRef>> {
        val startTimestamp = currentTimeMillis()

        val nodeRefs = nodeDescriptors.toNodeRefs()
        val nodesChecksum = nodesChecksumGenerator.generateChecksum(nodeRefs)

        val userName = authorizationService.getCurrentUser()

        return transform(determineTransformationDescriptor(transformation, nodeDescriptors))
            .doOnNext { verifyConsistency(nodeRefs, nodesChecksum) }
            .map { (_, transformedDataDescriptor) ->
                authorizationService.runAs(userName)
                { transformedDataDescriptorSaver.save(transformation, nodeRefs, transformedDataDescriptor) }
            }
            .doOnNext { resultNodeRefs ->
                logger.transformedSuccessfully(transformation, nodeDescriptors, resultNodeRefs, startTimestamp, currentTimeMillis())
            }
            .doOnError { exception -> handleError(transformation, nodeDescriptors, nodesChecksum, exception) }
            .decideIfRetry(retry, AnotherTransformationIsInProgressException::class.java) {
                logger.logOnRetry(transformation, nodeDescriptors, it, retry.maxAttempts, retry.nextAttemptDelay)
            }
            .cache() // to prevent making request many times - another subscribers will receive only list of node refs
    }

    private fun determineTransformationDescriptor(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>): TransformationDescriptor =
        transformationDescriptor(transformation, dataDescriptorGetter.get(nodeDescriptors), externalCommunicationParameters)

    private fun verifyConsistency(nodeRefs: List<NodeRef>, nodesChecksum: String) {
        val currentNodesChecksum = nodesChecksumGenerator.generateChecksum(nodeRefs)
        if (currentNodesChecksum != nodesChecksum) {
            throw NodesInconsistencyException(nodeRefs, nodesChecksum, currentNodesChecksum)
        }
    }

    private fun handleError(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, nodesChecksum: String, exception: Throwable) {
        if (exception is NodesInconsistencyException) {
            logger.skippedSavingResult(transformation, nodeDescriptors, exception.oldNodesChecksum, exception.currentNodesChecksum)

            throw AnotherTransformationIsInProgressException(transformation, nodeDescriptors, exception.oldNodesChecksum, exception.currentNodesChecksum)
        }

        val currentNodesChecksum = nodesChecksumGenerator.generateChecksum(nodeDescriptors.toNodeRefs())
        if (nodesChecksum != currentNodesChecksum) {
            logger.couldNotTransformButChecksumsAreDifferent(transformation, nodeDescriptors, nodesChecksum, currentNodesChecksum, exception)

            throw AnotherTransformationIsInProgressException(transformation, nodeDescriptors, nodesChecksum, currentNodesChecksum)
        } else {
            // private static inner classes aren't accessible (popular attitude in Reactor) - it causes that exception isn't printed by logger.
            // unwrapping it to get the essence of the exception
            if (exception.isInstanceOfInnerPrivateStaticClass()) {
                logger.couldNotTransform(transformation, nodeDescriptors, exception.cause!!)
            } else {
                logger.couldNotTransform(transformation, nodeDescriptors, exception)
            }

            throw exception
        }
    }

    private fun <T> Mono<T>.decideIfRetry(retry: Retry, allButClass: Class<out Throwable>, doOnRetry: (iteration: Long) -> Unit): Mono<T> =
        if (retry != Retry.No) {
            retryOnError(retry.maxAttempts, retry.nextAttemptDelay, allButClass, doOnRetry)
        } else {
            this
        }
}
