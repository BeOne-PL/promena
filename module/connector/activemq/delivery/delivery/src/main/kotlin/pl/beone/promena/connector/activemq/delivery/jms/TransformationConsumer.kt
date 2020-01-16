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
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
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

    /**
     * Serialization and deserialization is executed by
     * [KryoMessageConverter][pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter] automatically.
     *
     * The flow:
     * 1. Receives a message with serialized data from `${promena.connector.activemq.consumer.queue.request}` queue.
     *    It gets only messages for the transformers that are included in Promena
     *    ([transformationHashCode] is one of `${promena.connector.activemq.consumer.queue.request.message-selector}` values determined by
     *    [TransformationHashFunctionMessageSelectorDeterminer][pl.beone.promena.connector.activemq.configuration.delivery.jms.TransformationHashFunctionMessageSelectorDeterminer])
     * 2. Deserializes to [TransformationDescriptor]
     * 3. Performs a transformation
     * 4. Determines headers to send: [TRANSFORMATION_HASH_CODE], [TRANSFORMATION_START_TIMESTAMP], [TRANSFORMATION_END_TIMESTAMP],
     *    headers returned by [HeadersToSentBackDeterminer],
     *    and [KryoMessageConverter.PROPERTY_SERIALIZATION_CLASS][pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter.PROPERTY_SERIALIZATION_CLASS]
     * 5. Serializes to [PerformedTransformationDescriptor]
     * 6. Sends a message with serialized data as the body and [correlationId] id to [responseQueue] queue using [transformerProducer]
     * 7. In case of an error, it serializes an exception and sends a message with serialized data as the body and [correlationId] id
     *    to [errorResponseQueue] queue using [transformerProducer].
     */
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
            responseQueue to performedTransformationDescriptor(transformationUseCase.transform(transformation, dataDescriptor, communicationParameters))
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