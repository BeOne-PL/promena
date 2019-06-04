package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getAndVerifyLocation
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredProperty
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import java.util.*

@Configuration
class TransformerSenderContext {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerSenderContext::class.java)
    }

    @Bean
    fun transformerSender(@Qualifier("global-properties") properties: Properties,
                          jmsTemplate: JmsTemplate) =
            TransformerSender(properties.getAndVerifyLocation(logger),
                              ActiveMQQueue(properties.getRequiredProperty("promena.client.message-broker.consumer.queue.request")),
                              jmsTemplate)
}