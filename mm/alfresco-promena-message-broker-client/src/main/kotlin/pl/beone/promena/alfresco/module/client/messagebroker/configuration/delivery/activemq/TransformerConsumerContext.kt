package pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerConsumer
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager

@Configuration
class TransformerConsumerContext {

    @Bean
    fun transformerConsumer(alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                            completedTransformationManager: CompletedTransformationManager) =
            TransformerConsumer(alfrescoTransformedDataDescriptorSaver, completedTransformationManager)
}