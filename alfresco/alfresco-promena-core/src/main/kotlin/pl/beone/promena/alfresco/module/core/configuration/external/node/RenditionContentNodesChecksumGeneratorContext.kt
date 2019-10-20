package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.node.RenditionContentNodesChecksumGenerator

@Configuration
class RenditionContentNodesChecksumGeneratorContext {

    @Bean
    fun renditionContentNodesChecksumGenerator(
        serviceRegistry: ServiceRegistry
    ) =
        RenditionContentNodesChecksumGenerator(
            serviceRegistry
        )
}