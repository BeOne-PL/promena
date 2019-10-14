package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseConsumer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.TransformedDataDescriptorSaver

@Configuration
class TransformerResponseConsumerContext {

    @Bean
    fun transformerResponseConsumer(
        nodesChecksumGenerator: NodesChecksumGenerator,
        transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
        authorizationService: AuthorizationService,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformationParametersSerializationService: TransformationParametersSerializationService
    ): TransformerResponseConsumer =
        TransformerResponseConsumer(
            nodesChecksumGenerator,
            transformedDataDescriptorSaver,
            authorizationService,
            reactiveTransformationManager,
            transformationParametersSerializationService
        )
}