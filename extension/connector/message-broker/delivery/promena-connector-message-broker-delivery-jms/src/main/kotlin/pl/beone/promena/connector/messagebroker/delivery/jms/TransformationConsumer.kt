package pl.beone.promena.connector.messagebroker.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.connector.messagebroker.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase

class TransformationConsumer(jmsTemplate: JmsTemplate,
                             private val responseQueue: ActiveMQQueue,
                             private val errorResponseQueue: ActiveMQQueue,
                             private val transformationUseCase: TransformationUseCase) {

    private val communicationParametersConverter = CommunicationParametersConverter()
    private val headersToSentBackDeterminer = HeadersToSentBackDeterminer()
    private val transformerProducer = TransformationProducer(jmsTemplate)

    @JmsListener(destination = "\${promena.connector.message-broker.consumer.queue.request}",
                 selector = "\${promena.connector.message-broker.consumer.queue.request.message-selector}")
    fun receiveQueue(@Header(JmsHeaders.CORRELATION_ID) correlationId: String,
                     @Header(PromenaJmsHeaders.TRANSFORMATION_ID) transformationId: String,
                     @Headers headers: Map<String, Any>,
                     @Payload transformationDescriptor: TransformationDescriptor) {
        val startTimestamp = getTimestamp()

        val (queue, payload) = try {
            responseQueue to transformationUseCase.transform(transformationDescriptor.transformation,
                                                             transformationDescriptor.dataDescriptor,
                                                             communicationParametersConverter.convert(headers))
        } catch (e: Exception) {
            errorResponseQueue to e
        }

        val headersToSend = headersToSentBackDeterminer.determine(headers) +
                            (PromenaJmsHeaders.TRANSFORMATION_ID to transformationId) +
                            determineTimestampHeaders(startTimestamp, getTimestamp())

        transformerProducer.send(queue, correlationId, headersToSend, payload)
    }

    private fun determineTimestampHeaders(startTimestamp: Long, endTimestamp: Long): Map<String, Long> =
        mapOf(PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP to startTimestamp,
              PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP to endTimestamp)

    private fun getTimestamp(): Long =
        System.currentTimeMillis()
}