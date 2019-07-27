package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import org.springframework.jms.core.JmsTemplate
import java.time.Duration

class JmsQueueUtils(
    private val jmsTemplate: JmsTemplate
) {
    fun dequeueQueue(queue: String, timeout: Duration = Duration.ofMillis(300)) {
        val receiveTimeout = jmsTemplate.receiveTimeout
        jmsTemplate.receiveTimeout = timeout.toMillis()
        while (jmsTemplate.receive(queue) != null) {

        }
        jmsTemplate.receiveTimeout = receiveTimeout
    }
}