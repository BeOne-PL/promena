package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaAlfrescoJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.extension.couldNotTransform
import pl.beone.promena.alfresco.module.core.extension.couldNotTransformButChecksumsAreDifferent
import pl.beone.promena.alfresco.module.core.extension.logOnRetry
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono

class TransformerResponseErrorConsumer(
    private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
    private val alfrescoAuthenticationService: AlfrescoAuthenticationService,
    private val reactiveTransformationManager: ReactiveTransformationManager,
    private val activeMQAlfrescoPromenaTransformer: ActiveMQAlfrescoPromenaTransformer,
    private val transformationParametersSerializationService: TransformationParametersSerializationService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @JmsListener(
        destination = "\${promena.connector.activemq.consumer.queue.response.error}",
        selector = "\${promena.connector.activemq.consumer.queue.response.error.selector}"
    )
    fun receiveQueue(
        @Header(CORRELATION_ID) correlationId: String,
        @Header(SEND_BACK_TRANSFORMATION_PARAMETERS) transformationParameters: String,
        @Payload transformationException: TransformationException
    ) {
        val (nodeDescriptors, nodesChecksum, retry, attempt, userName) = transformationParametersSerializationService.deserialize(transformationParameters)

        val transformation = transformationException.transformation

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeDescriptors.toNodeRefs())
        if (nodesChecksum != currentNodesChecksum) {
            reactiveTransformationManager.completeErrorTransformation(
                correlationId,
                AnotherTransformationIsInProgressException(
                    transformation,
                    nodeDescriptors,
                    nodesChecksum,
                    currentNodesChecksum
                )
            )

            logger.couldNotTransformButChecksumsAreDifferent(transformation, nodeDescriptors, nodesChecksum, currentNodesChecksum, transformationException)
        } else {
            logger.couldNotTransform(transformation, nodeDescriptors, transformationException)

            if (wasLastAttempt(attempt, retry.maxAttempts)) {
                reactiveTransformationManager.completeErrorTransformation(correlationId, transformationException)
            } else {
                retry(correlationId, transformation, nodeDescriptors, retry, attempt + 1, userName)
            }
        }
    }

    private fun retry(id: String, transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, retry: Retry, attempt: Long, userName: String) {
        Mono.just("")
            .doOnNext { logger.logOnRetry(transformation, nodeDescriptors, attempt, retry.maxAttempts, retry.nextAttemptDelay) }
            .delayElement(retry.nextAttemptDelay)
            .doOnNext {
                alfrescoAuthenticationService.runAs(userName) {
                    activeMQAlfrescoPromenaTransformer.transformAsync(id, transformation, nodeDescriptors, retry, attempt)
                }
            }
            .subscribe()
    }

    private fun wasLastAttempt(attempt: Long, retryMaxAttempts: Long): Boolean =
        attempt == retryMaxAttempts
}