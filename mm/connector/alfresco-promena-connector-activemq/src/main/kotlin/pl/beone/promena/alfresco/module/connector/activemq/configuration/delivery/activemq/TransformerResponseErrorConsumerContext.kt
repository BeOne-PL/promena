package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator

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
