package pl.beone.promena.connector.activemq.configuration

import mu.KotlinLogging
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
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Registered <activemq> connector: <broker-url: ${activeMQProperties.brokerUrl}>, " +
                    "<pool.max-connections: ${activeMQProperties.pool.maxConnections}>, " +
                    "<listener.concurrency: ${jmsProperties.listener.concurrency}>, " +
                    "<listener.max-concurrency: ${jmsProperties.listener.maxConcurrency}>"
        }
    }
}