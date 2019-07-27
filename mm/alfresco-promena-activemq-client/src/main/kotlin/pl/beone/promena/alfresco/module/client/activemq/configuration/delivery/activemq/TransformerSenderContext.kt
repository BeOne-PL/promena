package pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.activemq.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import java.util.*

@Configuration
class TransformerSenderContext {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerSenderContext::class.java)
    }

    @Bean
    fun transformerSender(
        @Qualifier("global-properties") properties: Properties,
        externalCommunication: ExternalCommunication,
        transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
        jmsTemplate: JmsTemplate
    ) =
        TransformerSender(
            externalCommunication,
            transformationHashFunctionDeterminer,
            ActiveMQQueue(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.activemq.consumer.queue.request")),
            jmsTemplate
        )
}