package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.JmsUtils
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.TransformedDataDescriptorSaver

@Configuration
@ComponentScan(
    "pl.beone.promena.alfresco.module.core.configuration.internal",
    "pl.beone.promena.alfresco.module.connector.activemq.configuration.autoconfigure",
    "pl.beone.promena.alfresco.module.connector.activemq.configuration.internal",
    "pl.beone.promena.alfresco.module.connector.activemq.configuration.framework",
    "pl.beone.promena.alfresco.module.connector.activemq.configuration.external.springmessaging",
    "pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq"
)
class SetupContext {

    @Bean
    fun jmsQueueUtils(
        jmsTemplate: JmsTemplate,
        transformationParametersSerializationService: TransformationParametersSerializationService,
        environment: Environment
    ) =
        JmsUtils(
            jmsTemplate,
            transformationParametersSerializationService,
            environment.getRequiredProperty("promena.connector.activemq.consumer.queue.request"),
            environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response"),
            environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response.error")
        )

    @Bean
    fun nodesChecksumGenerator() =
        mockk<NodesChecksumGenerator>()

    @Bean
    fun transformedDataDescriptorSaver() =
        mockk<TransformedDataDescriptorSaver>()

    @Bean
    fun authorizationService() =
        mockk<AuthorizationService>()

    @Bean
    fun activeMQPromenaTransformer() =
        mockk<ActiveMQPromenaTransformer>()
}