package pl.beone.promena.connector.activemq.configuration

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.jms.JmsProperties
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import javax.annotation.PostConstruct

@Configuration
@DependsOn("transformationConsumer")
class ActiveMQConnectorLogger(
    private val environment: Environment,
    private val activeMQProperties: ActiveMQProperties,
    private val jmsProperties: JmsProperties
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Registered <activemq> connector: [<broker-url: ${activeMQProperties.brokerUrl}>, " +
                    "listener: <concurrency: ${jmsProperties.listener.concurrency}, max-concurrency: ${jmsProperties.listener.maxConcurrency}>, " +
                    "queue: <request: ${environment.getRequiredProperty("promena.connector.activemq.consumer.queue.request")}, " +
                    "response: ${environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response")}, " +
                    "response.error: ${environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response.error")}>]"
        }
    }
}