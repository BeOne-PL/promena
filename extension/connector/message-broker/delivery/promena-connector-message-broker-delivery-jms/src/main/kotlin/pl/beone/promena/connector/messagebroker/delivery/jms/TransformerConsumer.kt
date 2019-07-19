package pl.beone.promena.connector.messagebroker.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationNotFoundException

class TransformerConsumer(jmsTemplate: JmsTemplate,
                          private val responseQueue: ActiveMQQueue,
                          private val errorResponseQueue: ActiveMQQueue,
                          private val transformationUseCase: TransformationUseCase) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerConsumer::class.java)
    }

    private val communicationParametersConverter = CommunicationParametersConverter()
    private val headersToSentBackDeterminer = HeadersToSentBackDeterminer()
    private val transformerProducer = TransformerProducer(jmsTemplate)

    @JmsListener(destination = "\${promena.connector.message-broker.consumer.queue.request}",
                 selector = "\${promena.connector.message-broker.consumer.queue.request.selector}")
    fun receiveQueue(@Header(JmsHeaders.CORRELATION_ID) correlationId: String,
                     @Header(PromenaJmsHeader.PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Headers headers: Map<String, Any>,
                     @Payload transformationDescriptor: TransformationDescriptor) {
        val startTimestamp = getTimestamp()

        val (queue, payload) = try {
            val communicationParameters = communicationParametersConverter.convert(headers)

            responseQueue to transformationUseCase.transform(transformerId, transformationDescriptor, communicationParameters)
        } catch (e: TransforN) {
            throw e
        } catch (e: Exception) {
            errorResponseQueue to e
        }

        val headersToSend = headersToSentBackDeterminer.determine(headers) +
                            (PromenaJmsHeader.PROMENA_TRANSFORMER_ID to transformerId) +
                            determineTimestampHeaders(startTimestamp, getTimestamp())

        transformerProducer.send(queue, correlationId, headersToSend, payload)
    }

    private fun determineTimestampHeaders(startTimestamp: Long, endTimestamp: Long): Map<String, Long> =
            mapOf(PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP to startTimestamp,
                  PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP to endTimestamp)

    private fun getTimestamp(): Long =
            System.currentTimeMillis()
}