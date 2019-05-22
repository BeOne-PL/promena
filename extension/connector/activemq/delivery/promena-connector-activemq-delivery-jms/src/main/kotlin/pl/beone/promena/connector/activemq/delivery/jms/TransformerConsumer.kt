package pl.beone.promena.connector.activemq.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import java.lang.RuntimeException

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

    @JmsListener(destination = "\${activemq.promena.consumer.queue.request}",
                 selector = "\${activemq.promena.consumer.selector}")
    fun receiveQueue(@Header(JmsHeaders.CORRELATION_ID) correlationId: String,
                     @Header(TransformerJmsHeader.PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Headers headers: Map<String, Any>,
                     @Payload transformationDescriptor: TransformationDescriptor) {
        val startTimestamp = getTimestamp()

        val (queue, payload) = try {
            val communicationParameters = communicationParametersConverter.convert(headers)

            responseQueue to transformationUseCase.transform(transformerId,
                                                             transformationDescriptor,
                                                             communicationParameters)
        } catch (e: Exception) {
            logException(e, transformerId, transformationDescriptor)

            errorResponseQueue to e
        }

        val headersToSend = headersToSentBackDeterminer.determine(headers) +
                            (TransformerJmsHeader.PROMENA_TRANSFORMER_ID to transformerId) +
                            determineTimestampHeaders(startTimestamp, getTimestamp())

        transformerProducer.send(queue,
                                 correlationId,
                                 headersToSend,
                                 payload)
    }

    private fun logException(e: Exception, transformerId: String, transformationDescriptor: TransformationDescriptor) {
        logger.error("An error occurred during transforming <{}> <{}> <{}> <{}>",
                     transformerId,
                     transformationDescriptor.dataDescriptors.getLocationsInString(),
                     transformationDescriptor.parameters,
                     transformationDescriptor.targetMediaType,
                     e)
    }

    private fun List<DataDescriptor>.getLocationsInString(): String =
            joinToString(",") {
                try {
                    "${it.data.getLocation()}, ${it.mediaType}"
                } catch (e: UnsupportedOperationException) {
                    "no location, ${it.mediaType}"
                }
            }


    private fun determineTimestampHeaders(startTimestamp: Long, endTimestamp: Long): Map<String, Long> =
            mapOf(TransformerJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP to startTimestamp,
                  TransformerJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP to endTimestamp)

    private fun getTimestamp(): Long =
            System.currentTimeMillis()
}