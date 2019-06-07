package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerResponseConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager

@Configuration
class TransformerResponseConsumerContext {

    @Bean
    fun transformerResponseConsumer(alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                    alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                                    completedTransformationManager: CompletedTransformationManager): TransformerResponseConsumer =
            TransformerResponseConsumer(alfrescoNodesChecksumGenerator, alfrescoTransformedDataDescriptorSaver, completedTransformationManager)
}