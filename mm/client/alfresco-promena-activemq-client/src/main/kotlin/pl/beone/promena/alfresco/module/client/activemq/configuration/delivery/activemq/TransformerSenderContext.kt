package pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.util.*

@Configuration
class TransformerSenderContext {

    @Bean
    fun transformerSender(
        @Qualifier("global-properties") properties: Properties,
        transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
        alfrescoAuthenticationService: AlfrescoAuthenticationService,
        jmsTemplate: JmsTemplate
    ) =
        TransformerSender(
            transformationHashFunctionDeterminer,
            alfrescoAuthenticationService,
            ActiveMQQueue(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.activemq.consumer.queue.request")),
            jmsTemplate
        )
}