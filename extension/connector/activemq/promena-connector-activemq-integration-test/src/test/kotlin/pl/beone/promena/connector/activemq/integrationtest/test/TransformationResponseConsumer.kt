package pl.beone.promena.connector.activemq.integrationtest.test

import org.springframework.jms.annotation.JmsListener
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Component
class TransformationResponseConsumer {

    private val messages = LinkedBlockingQueue<HeadersWithPayload<PerformedTransformationDescriptor>>()
    private val errorMessages = LinkedBlockingQueue<HeadersWithPayload<Exception>>()

    @JmsListener(destination = "\${promena.connector.activemq.consumer.queue.response}")
    fun receiveQueue(@Headers headers: Map<String, Any>,
                     @Payload performedTransformationDescriptor: PerformedTransformationDescriptor) {
        messages.add(HeadersWithPayload(headers, performedTransformationDescriptor))
    }

    @JmsListener(destination = "\${promena.connector.activemq.consumer.queue.response.error}")
    fun receiveErrorQueue(@Headers headers: Map<String, Any>,
                          @Payload exception: Exception) {
        errorMessages.add(HeadersWithPayload(headers, exception))
    }

    fun getMessage(maxWait: Long): HeadersWithPayload<PerformedTransformationDescriptor> =
        messages.poll(maxWait, TimeUnit.MILLISECONDS) ?: throw  IllegalStateException()

    fun getErrorMessage(maxWait: Long): HeadersWithPayload<Exception> =
        errorMessages.poll(maxWait, TimeUnit.MILLISECONDS) ?: throw  IllegalStateException()
}