package pl.beone.promena.connector.activemq.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import javax.jms.Message

internal class TransformationProducer(
    private val jmsTemplate: JmsTemplate
) {

    /**
     * @see TransformationConsumer
     */
    fun send(queue: ActiveMQQueue, correlationId: String, headers: Map<String, Any>, payload: Any) {
        jmsTemplate.convertAndSend(queue, payload) { message ->
            message.apply {
                jmsCorrelationID = correlationId

                setHeaders(headers)
            }
        }
    }

    private fun Message.setHeaders(headersToSentBack: Map<String, Any>) {
        headersToSentBack.forEach { setProperty(it.key, it.value) }
    }

    private fun Message.setProperty(key: String, value: Any) {
        when (value::class) {
            Boolean::class -> setBooleanProperty(key, value as Boolean)
            Byte::class -> setByteProperty(key, value as Byte)
            Int::class -> setIntProperty(key, value as Int)
            Long::class -> setLongProperty(key, value as Long)
            Float::class -> setFloatProperty(key, value as Float)
            Double::class -> setDoubleProperty(key, value as Double)
            String::class -> setStringProperty(key, value as String)
            else -> setObjectProperty(key, value)
        }
    }
}