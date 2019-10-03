package pl.beone.promena.alfresco.module.connector.activemq.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.util.*

@Configuration
class ActiveMQAlfrescoPromenaTransformerContext {

    @Bean
    fun activeMQAlfrescoPromenaTransformer(
        @Qualifier("global-properties") properties: Properties,
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        retry: Retry,
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformerSender: TransformerSender
    ) =
        ActiveMQAlfrescoPromenaTransformer(
            externalCommunicationParameters,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        )
}