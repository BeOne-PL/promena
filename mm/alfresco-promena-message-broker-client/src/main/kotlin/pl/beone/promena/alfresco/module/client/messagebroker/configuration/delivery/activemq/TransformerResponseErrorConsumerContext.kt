package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredProperty
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.toDuration
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import java.util.*

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(@Qualifier("global-properties") properties: Properties,
                                         completedTransformationManager: CompletedTransformationManager,
                                         activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService): TransformerResponseErrorConsumer =
            TransformerResponseErrorConsumer(properties.getRequiredProperty("promena.transformation.error.tryAgain").toBoolean(),
                                             properties.getRequiredProperty("promena.transformation.error.delay").let { it.toDuration() },
                                             completedTransformationManager,
                                             activeMQAlfrescoPromenaService)
}