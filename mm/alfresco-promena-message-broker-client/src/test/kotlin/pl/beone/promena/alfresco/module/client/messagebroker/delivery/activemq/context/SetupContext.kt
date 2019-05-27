package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context

import io.mockk.mockk
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoTransformedDataDescriptorSaver

@Configuration
@ComponentScan(
        "pl.beone.promena.alfresco.module.client.messagebroker.boot",
        "pl.beone.promena.alfresco.module.client.messagebroker.configuration.internal",
        "pl.beone.promena.alfresco.module.client.messagebroker.configuration.framework",
        "pl.beone.promena.alfresco.module.client.messagebroker.configuration.delivery.activemq"
)
class SetupContext {

    @Bean
    fun alfrescoTransformedDataDescriptorSaver() =
            mockk<AlfrescoTransformedDataDescriptorSaver>()
}