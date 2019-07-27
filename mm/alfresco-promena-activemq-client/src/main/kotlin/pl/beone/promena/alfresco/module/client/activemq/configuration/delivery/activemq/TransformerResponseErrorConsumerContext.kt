package pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator

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
