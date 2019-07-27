package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        reactiveTransformationManager: ReactiveTransformationManager,
        activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService
    ): TransformerResponseErrorConsumer =
        TransformerResponseErrorConsumer(
            alfrescoNodesChecksumGenerator,
            reactiveTransformationManager,
            activeMQAlfrescoPromenaService
        )
}
