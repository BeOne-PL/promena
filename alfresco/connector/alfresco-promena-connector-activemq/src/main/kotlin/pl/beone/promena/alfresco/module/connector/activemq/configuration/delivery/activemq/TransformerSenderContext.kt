package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import java.util.*

@Configuration
class TransformerSenderContext {

    @Bean
    fun transformerSender(
        @Qualifier("global-properties") properties: Properties,
        transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
        transformationParametersSerializationService: TransformationParametersSerializationService,
        jmsTemplate: JmsTemplate
    ) =
        TransformerSender(
            transformationHashFunctionDeterminer,
            transformationParametersSerializationService,
            ActiveMQQueue(properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.activemq.consumer.queue.request")),
            jmsTemplate
        )
}