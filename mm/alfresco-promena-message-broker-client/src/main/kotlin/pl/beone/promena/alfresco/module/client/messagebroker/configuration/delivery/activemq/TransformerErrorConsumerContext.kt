package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerErrorConsumer

@Configuration
class TransformerErrorConsumerContext {

    @Bean
    fun transformerErrorConsumer(): TransformerErrorConsumer =
            TransformerErrorConsumer()
}