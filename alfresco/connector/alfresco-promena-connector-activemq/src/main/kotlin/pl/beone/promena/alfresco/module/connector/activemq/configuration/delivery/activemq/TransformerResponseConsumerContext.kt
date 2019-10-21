package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseConsumer
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseProcessor
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.TransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager

@Configuration
class TransformerResponseConsumerContext {

    @Bean
    fun transformerResponseConsumer(
        promenaMutableTransformationManager: PromenaMutableTransformationManager,
        transformerResponseProcessor: TransformerResponseProcessor,
        transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
        transformationParametersSerializationService: TransformationParametersSerializationService,
        authorizationService: AuthorizationService,
        serviceRegistry: ServiceRegistry
    ) =
        TransformerResponseConsumer(
            promenaMutableTransformationManager,
            transformerResponseProcessor,
            transformedDataDescriptorSaver,
            transformationParametersSerializationService,
            authorizationService,
            serviceRegistry
        )
}