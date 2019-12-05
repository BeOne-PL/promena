package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import java.time.Duration
import javax.jms.Message

class JmsUtils(
    private val jmsTemplate: JmsTemplate,
    private val transformationParametersSerializationService: TransformationParametersSerializationService,
    private val queueRequest: String,
    private val queueResponse: String,
    private val queueResponseError: String
) {

    fun dequeueQueues(timeout: Duration = Duration.ofMillis(250)) {
        dequeueQueue(queueRequest, timeout)
        dequeueQueue(queueResponse, timeout)
        dequeueQueue(queueResponseError, timeout)
    }

    private fun dequeueQueue(queue: String, timeout: Duration) {
        val receiveTimeout = jmsTemplate.receiveTimeout
        jmsTemplate.receiveTimeout = timeout.toMillis()
        while (jmsTemplate.receive(queue) != null) {
            // deliberately omitted
        }
        jmsTemplate.receiveTimeout = receiveTimeout
    }

    fun sendResponseMessage(
        executionId: String,
        performedTransformationDescriptor: PerformedTransformationDescriptor,
        transformationParameters: TransformationParameters
    ) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponse), performedTransformationDescriptor) {
            it.setCommonHeaders(executionId, transformationParameters)
        }
    }

    fun sendResponseErrorMessage(executionId: String, exception: Exception, transformationParameters: TransformationParameters) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponseError), exception) {
            it.setCommonHeaders(executionId, transformationParameters)
        }
    }

    private fun Message.setCommonHeaders(executionId: String, transformationParameters: TransformationParameters): Message {
        jmsCorrelationID = executionId
        setLongProperty(TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
        setLongProperty(TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis() + Duration.ofDays(1).toMillis())

        setStringProperty(SEND_BACK_TRANSFORMATION_PARAMETERS, transformationParametersSerializationService.serialize(transformationParameters))

        setLongProperty(SEND_BACK_ATTEMPT, transformationParameters.attempt)
        setLongProperty(SEND_BACK_RETRY_MAX_ATTEMPTS, determineMaxAttempts(transformationParameters))

        return this
    }

    private fun determineMaxAttempts(transformationParameters: TransformationParameters): Long =
        if (transformationParameters.retry !is Retry.No) {
            transformationParameters.retry.maxAttempts
        } else {
            0
        }
}