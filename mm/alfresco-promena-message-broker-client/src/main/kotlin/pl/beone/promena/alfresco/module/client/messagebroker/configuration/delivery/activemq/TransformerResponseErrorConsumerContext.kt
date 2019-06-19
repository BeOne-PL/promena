package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.toDuration
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import java.util.*

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(@Qualifier("global-properties") properties: Properties,
                                         alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                         reactiveTransformationManager: ReactiveTransformationManager,
                                         activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService): TransformerResponseErrorConsumer =
            TransformerResponseErrorConsumer(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.enabled").toBoolean(),
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.max-attempts").toLong(),
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.next-attempt-delay").toDuration(),
                                             alfrescoNodesChecksumGenerator,
                                             reactiveTransformationManager,
                                             activeMQAlfrescoPromenaService)
}