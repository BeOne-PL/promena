package pl.beone.promena.alfresco.module.connector.activemq.configuration

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure.jms.JmsProperties
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import java.util.*
import javax.annotation.PostConstruct

@Configuration
@DependsOn("transformerResponseConsumer", "transformerResponseErrorConsumer", "transformerSender")
class ActiveMQConnectorLogger(
    @Qualifier("global-properties") private val properties: Properties,
    private val jmsProperties: JmsProperties
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Registered <activemq> connector: [<broker-url: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.spring.activemq.broker-url")}>, " +
                    "listener: <concurrency: ${jmsProperties.listener.concurrency}, max-concurrency: ${jmsProperties.listener.maxConcurrency}>, " +
                    "queue: <request: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.consumer.queue.request")}, " +
                    "response: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.consumer.queue.response")}, " +
                    "response.error: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.consumer.queue.response.error")}>, " +
                    "response.error.selector: ${properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.consumer.queue.response.error.selector")}>]"
        }
    }
}