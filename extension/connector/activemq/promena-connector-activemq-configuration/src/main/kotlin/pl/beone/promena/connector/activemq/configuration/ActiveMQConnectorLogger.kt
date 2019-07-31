package pl.beone.promena.connector.activemq.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jms.JmsProperties
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.annotation.PostConstruct

@Configuration
@DependsOn("transformationConsumer")
class ActiveMQConnectorLogger(
    private val activeMQProperties: ActiveMQProperties,
    private val jmsProperties: JmsProperties
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ActiveMQConnectorLogger::class.java)
    }

    @PostConstruct
    private fun log() {
        logger.info(
            "Registered <activemq> connector: <broker-url: {}>, <pool.max-connections: {}>, <listener.concurrency: {}>, <listener.max-concurrency: {}>",
            activeMQProperties.brokerUrl,
            activeMQProperties.pool.maxConnections,
            jmsProperties.listener.concurrency,
            jmsProperties.listener.maxConcurrency
        )
    }
}