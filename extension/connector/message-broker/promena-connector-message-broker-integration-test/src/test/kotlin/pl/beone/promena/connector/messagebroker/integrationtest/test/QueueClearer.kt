package pl.beone.promena.connector.messagebroker.integrationtest.test

import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import javax.jms.Message

@Component
class QueueClearer(private val jmsTemplate: JmsTemplate,
                   @Value("\${promena.connector.message-broker.consumer.queue.request}") private val queueRequest: String,
                   @Value("\${promena.connector.message-broker.consumer.queue.response}") private val queueResponse: String,
                   @Value("\${promena.connector.message-broker.consumer.queue.response.error}") private val queueResponseError: String) {

    fun clearQueues() {
        clearQueue(queueRequest)
        clearQueue(queueResponse)
        clearQueue(queueResponseError)
    }

    private fun clearQueue(queue: String) {
        val receiveTimeout = jmsTemplate.receiveTimeout
        jmsTemplate.receiveTimeout = 500
        var message: Message?
        do {
            message = jmsTemplate.receive(queue)
        } while (message != null)
        jmsTemplate.receiveTimeout = receiveTimeout
    }
}