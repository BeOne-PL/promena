package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import java.util.*

@Configuration
class ActiveMQAlfrescoPromenaServiceContext {


    @Bean
    fun activeMQAlfrescoPromenaService(@Qualifier("global-properties") properties: Properties,
                                       completedTransformationManager: CompletedTransformationManager,
                                       alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                       transformerSender: TransformerSender) =
            ActiveMQAlfrescoPromenaService(completedTransformationManager,
                                           alfrescoDataDescriptorGetter,
                                           transformerSender)
}