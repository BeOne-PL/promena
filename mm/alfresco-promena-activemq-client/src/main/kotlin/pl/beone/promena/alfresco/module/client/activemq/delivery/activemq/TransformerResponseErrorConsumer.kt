package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.NodeRef
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.activemq.applicationmodel.PromenaAlfrescoJmsHeaders
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert.RetryConverter
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.common.couldNotTransform
import pl.beone.promena.alfresco.module.client.base.common.couldNotTransformButChecksumsAreDifferent
import pl.beone.promena.alfresco.module.client.base.common.logOnRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono

class TransformerResponseErrorConsumer(
    private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
    private val reactiveTransformationManager: ReactiveTransformationManager,
    private val activeMQAlfrescoPromenaTransformer: ActiveMQAlfrescoPromenaTransformer
) {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val nodeRefsConverter = NodeRefsConverter()
        private val retryConverter = RetryConverter()
    }

    @JmsListener(
        destination = "\${promena.client.activemq.consumer.queue.response.error}",
        selector = "\${promena.client.activemq.consumer.queue.response.error.selector}"
    )
    fun receiveQueue(
        @Header(CORRELATION_ID) correlationId: String,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT) attempt: Long,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_ENABLED) retryEnabled: Boolean,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS) retryMaxAttempts: Long,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY) retryNextAttemptDelay: String,
        @Payload transformationException: TransformationException
    ) {
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)
        val retry = retryConverter.convert(retryEnabled, retryMaxAttempts, retryNextAttemptDelay)

        val transformation = transformationException.transformation

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            reactiveTransformationManager.completeErrorTransformation(
                correlationId,
                AnotherTransformationIsInProgressException(transformation, nodeRefs, nodesChecksum, currentNodesChecksum)
            )
            logger.couldNotTransformButChecksumsAreDifferent(transformation, nodeRefs, nodesChecksum, currentNodesChecksum, transformationException)
        } else {
            logger.couldNotTransform(transformation, nodeRefs, transformationException)

            retry(correlationId, transformation, nodeRefs, retry, attempt + 1, transformationException)
        }
    }

    private fun retry(
        id: String,
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        retry: Retry,
        attempt: Long,
        exception: TransformationException
    ) {
        Mono.just("no matter")
            .doOnNext { logger.logOnRetry(transformation, nodeRefs, attempt, retry.maxAttempts, retry.nextAttemptDelay) }
            .delayElement(retry.nextAttemptDelay)
            .doOnNext {
                activeMQAlfrescoPromenaTransformer.transformAsync(id, transformation, nodeRefs, retry, attempt)
                    .subscribeAndCompleteErrorTransformationIfLastAttemptFails(id, retry, attempt, exception)
            }
            .subscribe()
    }

    private fun Mono<*>.subscribeAndCompleteErrorTransformationIfLastAttemptFails(
        id: String,
        retry: Retry,
        attempt: Long,
        exception: Exception
    ) {
        if (retry.lastAttempt(attempt)) {
            subscribe(
                {},
                { reactiveTransformationManager.completeErrorTransformation(id, exception) }
            )
        } else {
            subscribe()
        }
    }

    private fun Retry.lastAttempt(attempt: Long): Boolean =
        attempt == maxAttempts
}