package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.ActiveMQPromenaTransformationExecutor
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.extension.couldNotTransform
import pl.beone.promena.alfresco.module.core.extension.logOnRetry
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException

class TransformerResponseErrorConsumer(
    private val promenaMutableTransformationManager: PromenaMutableTransformationManager,
    private val transformerResponseProcessor: TransformerResponseProcessor,
    private val activeMQPromenaTransformer: ActiveMQPromenaTransformationExecutor,
    private val authorizationService: AuthorizationService,
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
        @Header(SEND_BACK_TRANSFORMATION_PARAMETERS) transformationParametersString: String,
        @Payload transformationException: TransformationException
    ) {
        val transformationExecution = transformationExecution(correlationId)

        val transformationParameters = transformationParametersSerializationService.deserialize(transformationParametersString)
        val (nodeDescriptor, _, retry, dataDescriptor, nodesChecksum, attempt, userName) = transformationParameters

        val transformation = transformationException.transformation

        transformerResponseProcessor.process(transformation, nodeDescriptor, transformationExecution, nodesChecksum) {
            if (retry is Retry.No || wasLastAttempt(attempt, retry.maxAttempts)) {
                logger.couldNotTransform(transformation, nodeDescriptor, transformationException)
                promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, transformationException)
            } else {
                val currentAttempt = attempt + 1

                logger.logOnRetry(transformation, nodeDescriptor, currentAttempt, retry.maxAttempts, retry.nextAttemptDelay, transformationException)
                Thread.sleep(retry.nextAttemptDelay.toMillis())

                try {
                    authorizationService.runAs(userName) {
                        activeMQPromenaTransformer.execute(
                            correlationId,
                            transformation,
                            dataDescriptor,
                            transformationParameters.copy(attempt = currentAttempt)
                        )
                    }
                } catch (e: Exception) {
                    logger.couldNotTransform(transformation, nodeDescriptor, e)
                    promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, e)
                }
            }
        }
    }

    private fun wasLastAttempt(attempt: Long, retryMaxAttempts: Long): Boolean =
        attempt == retryMaxAttempts
}