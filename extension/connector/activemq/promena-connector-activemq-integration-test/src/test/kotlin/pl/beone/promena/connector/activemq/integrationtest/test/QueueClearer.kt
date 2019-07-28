package pl.beone.promena.connector.activemq.integrationtest.test

import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class QueueClearer(
    private val jmsTemplate: JmsTemplate,
    @Value("\${promena.connector.activemq.consumer.queue.request}") private val queueRequest: String,
    @Value("\${promena.connector.activemq.consumer.queue.response}") private val queueResponse: String,
    @Value("\${promena.connector.activemq.consumer.queue.response.error}") private val queueResponseError: String
) {

    fun dequeueQueues() {
        dequeueQueue(queueRequest)
        dequeueQueue(queueResponse)
        dequeueQueue(queueResponseError)
    }

    private fun dequeueQueue(queue: String, timeout: Duration = Duration.ofMillis(300)) {
        val receiveTimeout = jmsTemplate.receiveTimeout
        jmsTemplate.receiveTimeout = timeout.toMillis()
        while (jmsTemplate.receive(queue) != null) {

        }
        jmsTemplate.receiveTimeout = receiveTimeout    }
}