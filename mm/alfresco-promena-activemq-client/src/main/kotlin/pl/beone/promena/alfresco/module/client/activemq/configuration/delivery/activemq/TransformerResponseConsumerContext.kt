package pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerResponseConsumer
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver

@Configuration
class TransformerResponseConsumerContext {

    @Bean
    fun transformerResponseConsumer(
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
        reactiveTransformationManager: ReactiveTransformationManager
    ): TransformerResponseConsumer =
        TransformerResponseConsumer(
            alfrescoNodesChecksumGenerator,
            alfrescoTransformedDataDescriptorSaver,
            reactiveTransformationManager
        )
}