package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.external.node.ContentPropertyDataDescriptorGetter

@Configuration
class ContentPropertyDataDescriptorGetterContext {

    @Bean
    fun contentPropertyDataDescriptorGetter(
        dataConverter: DataConverter,
        serviceRegistry: ServiceRegistry
    ) =
        ContentPropertyDataDescriptorGetter(
            dataConverter,
            serviceRegistry
        )
}