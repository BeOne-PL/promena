package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getAndVerifyLocation
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getPropertyWithEmptySupport
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredProperty
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.toDuration
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import java.util.*

@Configuration
class ActiveMQAlfrescoPromenaServiceContext {

    companion object {
        private val logger = LoggerFactory.getLogger(ActiveMQAlfrescoPromenaServiceContext::class.java)
    }

    @Bean
    fun activeMQAlfrescoPromenaService(@Qualifier("global-properties") properties: Properties,
                                       completedTransformationManager: CompletedTransformationManager,
                                       alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                       jmsTemplate: JmsTemplate) =
            ActiveMQAlfrescoPromenaService(properties.getAndVerifyLocation(logger),
                                           properties.getPropertyWithEmptySupport("promena.client.message-broker.waitMax")?.let {
                                               it.toDuration()
                                           },
                                           completedTransformationManager,
                                           alfrescoDataDescriptorGetter,
                                           ActiveMQQueue(properties.getRequiredProperty("promena.client.message-broker.consumer.queue.request")),
                                           jmsTemplate)
}