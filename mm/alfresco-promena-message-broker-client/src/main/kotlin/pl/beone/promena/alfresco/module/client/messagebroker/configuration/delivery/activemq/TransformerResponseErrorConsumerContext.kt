package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.toDuration
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import java.util.*

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(@Qualifier("global-properties") properties: Properties,
                                         alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                         completedTransformationManager: CompletedTransformationManager,
                                         activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService): TransformerResponseErrorConsumer =
            TransformerResponseErrorConsumer(properties.getRequiredPropertyWithResolvedPlaceholders("promena.transformation.error.tryAgain").toBoolean(),
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.transformation.error.delay").let { it.toDuration() },
                                             alfrescoNodesChecksumGenerator,
                                             completedTransformationManager,
                                             activeMQAlfrescoPromenaService)
}