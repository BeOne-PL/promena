package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.context

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.JmsQueueUtils
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver

@Configuration
@ComponentScan(
    "pl.beone.promena.alfresco.module.client.base.configuration.internal",
    "pl.beone.promena.alfresco.module.client.activemq.configuration.autoconfigure",
    "pl.beone.promena.alfresco.module.client.activemq.configuration.internal",
    "pl.beone.promena.alfresco.module.client.activemq.configuration.framework",
    "pl.beone.promena.alfresco.module.client.activemq.configuration.external.springmessaging",
    "pl.beone.promena.alfresco.module.client.activemq.configuration.delivery.activemq"
)
class SetupContext {

    @Bean
    fun jmsQueueUtils(jmsTemplate: JmsTemplate) =
        JmsQueueUtils(jmsTemplate)

    @Bean
    fun alfrescoNodesChecksumGenerator() =
        mockk<AlfrescoNodesChecksumGenerator>()

    @Bean
    fun alfrescoTransformedDataDescriptorSaver() =
        mockk<AlfrescoTransformedDataDescriptorSaver>()

    @Bean
    fun alfrescoAuthenticationService() =
        mockk<AlfrescoAuthenticationService>()

    @Bean
    fun activeMQAlfrescoPromenaService() =
        mockk<ActiveMQAlfrescoPromenaService>()
}