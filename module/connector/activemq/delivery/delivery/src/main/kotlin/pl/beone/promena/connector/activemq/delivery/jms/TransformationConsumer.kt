package pl.beone.promena.connector.activemq.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase

class TransformationConsumer(
    jmsTemplate: JmsTemplate,
    private val responseQueue: ActiveMQQueue,
    private val errorResponseQueue: ActiveMQQueue,
    private val transformationUseCase: TransformationUseCase
) {

    private val transformerProducer = TransformationProducer(jmsTemplate)

    @JmsListener(
        destination = "\${promena.connector.activemq.consumer.queue.request}",
        selector = "\${promena.connector.activemq.consumer.queue.request.message-selector}"
    )
    fun receiveQueue(
        @Header(CORRELATION_ID) correlationId: String,
        @Header(TRANSFORMATION_HASH_CODE) transformationHashCode: String,
        @Headers headers: Map<String, Any>,
        @Payload transformationDescriptor: TransformationDescriptor
    ) {
        val startTimestamp = getTimestamp()

        val (transformation, dataDescriptor, communicationParameters) = transformationDescriptor

        val (queue, payload) = try {
            responseQueue to performedTransformationDescriptor(
                transformation,
                transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
            )
        } catch (e: Exception) {
            errorResponseQueue to e
        }

        val headersToSend = HeadersToSentBackDeterminer.determine(headers) +
                (TRANSFORMATION_HASH_CODE to transformationHashCode) +
                determineTimestampHeaders(startTimestamp, getTimestamp())

        transformerProducer.send(queue, correlationId, headersToSend, payload)
    }

    private fun determineTimestampHeaders(startTimestamp: Long, endTimestamp: Long): Map<String, Long> =
        mapOf(
            TRANSFORMATION_START_TIMESTAMP to startTimestamp,
            TRANSFORMATION_END_TIMESTAMP to endTimestamp
        )

    private fun getTimestamp(): Long =
        System.currentTimeMillis()
}