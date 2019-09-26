package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseConsumer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.AlfrescoTransformedDataDescriptorSaver

@Configuration
class TransformerResponseConsumerContext {

    @Bean
    fun transformerResponseConsumer(
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
        alfrescoAuthenticationService: AlfrescoAuthenticationService,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformationParametersSerializationService: TransformationParametersSerializationService
    ): TransformerResponseConsumer =
        TransformerResponseConsumer(
            alfrescoNodesChecksumGenerator,
            alfrescoTransformedDataDescriptorSaver,
            alfrescoAuthenticationService,
            reactiveTransformationManager,
            transformationParametersSerializationService
        )
}