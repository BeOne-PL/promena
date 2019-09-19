package pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        activeMQAlfrescoPromenaTransformer: ActiveMQAlfrescoPromenaTransformer,
        alfrescoAuthenticationService: AlfrescoAuthenticationService,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformationParametersSerializationService: TransformationParametersSerializationService
    ): TransformerResponseErrorConsumer =
        TransformerResponseErrorConsumer(
            alfrescoNodesChecksumGenerator,
            alfrescoAuthenticationService,
            reactiveTransformationManager,
            activeMQAlfrescoPromenaTransformer,
            transformationParametersSerializationService
        )
}
