package pl.beone.promena.connector.messagebroker.integrationtest.test

import org.springframework.jms.annotation.JmsListener
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class TransformerResponseConsumer {

    private val messages = LinkedBlockingQueue<HeadersWithPayload<List<TransformedDataDescriptor>>>()
    private val errorMessages = LinkedBlockingQueue<HeadersWithPayload<Exception>>()

    @JmsListener(destination = "\${promena.connector.message-broker.consumer.queue.response}")
    fun receiveQueue(@Headers headers: Map<String, Any>,
                     @Payload transformedDataDescriptors: List<TransformedDataDescriptor>) {
        messages.add(HeadersWithPayload(headers, transformedDataDescriptors))
    }

    @JmsListener(destination = "\${promena.connector.message-broker.consumer.queue.response.error}")
    fun receiveErrorQueue(@Headers headers: Map<String, Any>,
                          @Payload exception: Exception) {
        errorMessages.add(HeadersWithPayload(headers, exception))
    }

    fun getMessage(maxWait: Long): HeadersWithPayload<List<TransformedDataDescriptor>> =
            messages.poll(maxWait, TimeUnit.MILLISECONDS)

    fun getErrorMessage(maxWait: Long): HeadersWithPayload<Exception> =
            errorMessages.poll(maxWait, TimeUnit.MILLISECONDS)
}