package pl.beone.promena.connector.activemq.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase

class TransformationConsumer(
    jmsTemplate: JmsTemplate,
    private val responseQueue: ActiveMQQueue,
    private val errorResponseQueue: ActiveMQQueue,
    private val transformationUseCase: TransformationUseCase
) {

    private val communicationParametersConverter = CommunicationParametersConverter()
    private val headersToSentBackDeterminer = HeadersToSentBackDeterminer()
    private val transformerProducer = TransformationProducer(jmsTemplate)

    @JmsListener(
        destination = "\${promena.connector.activemq.consumer.queue.request}",
        selector = "\${promena.connector.activemq.consumer.queue.request.message-selector}"
    )
    fun receiveQueue(
        @Header(JmsHeaders.CORRELATION_ID) correlationId: String,
        @Header(PromenaJmsHeaders.TRANSFORMATION_ID) transformationId: String,
        @Header(PromenaJmsHeaders.TRANSFORMATION_HASH_CODE) transformationHashCode: String,
        @Headers headers: Map<String, Any>,
        @Payload transformationDescriptor: TransformationDescriptor
    ) {
        val startTimestamp = getTimestamp()

        val (transformation, dataDescriptor) = transformationDescriptor

        val (queue, payload) = try {
            responseQueue to PerformedTransformationDescriptor.of(
                transformation,
                transformationUseCase.transform(transformation, dataDescriptor, communicationParametersConverter.convert(headers))
            )
        } catch (e: Exception) {
            errorResponseQueue to e
        }

        val headersToSend = headersToSentBackDeterminer.determine(headers) +
                (PromenaJmsHeaders.TRANSFORMATION_ID to transformationId) +
                (PromenaJmsHeaders.TRANSFORMATION_HASH_CODE to transformationHashCode) +
                determineTimestampHeaders(startTimestamp, getTimestamp())

        transformerProducer.send(queue, correlationId, headersToSend, payload)
    }

    private fun determineTimestampHeaders(startTimestamp: Long, endTimestamp: Long): Map<String, Long> =
        mapOf(
            PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP to startTimestamp,
            PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP to endTimestamp
        )

    private fun getTimestamp(): Long =
        System.currentTimeMillis()
}