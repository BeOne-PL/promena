package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import java.util.*

@Configuration
class ActiveMQAlfrescoPromenaServiceContext {


    @Bean
    fun activeMQAlfrescoPromenaService(
        @Qualifier("global-properties") properties: Properties,
        retry: Retry,
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformerSender: TransformerSender
    ) =
        ActiveMQAlfrescoPromenaService(
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        )
}