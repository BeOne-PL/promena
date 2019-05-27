package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(completedTransformationManager: CompletedTransformationManager): TransformerResponseErrorConsumer =
            TransformerResponseErrorConsumer(completedTransformationManager)
}