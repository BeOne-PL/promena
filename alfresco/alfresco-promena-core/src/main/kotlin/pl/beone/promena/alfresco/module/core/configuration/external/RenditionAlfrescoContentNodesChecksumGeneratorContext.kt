package pl.beone.promena.alfresco.module.core.configuration.external

import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.RenditionAlfrescoContentNodesChecksumGenerator

@Configuration
class RenditionAlfrescoContentNodesChecksumGeneratorContext {

    @Bean
    fun renditionAlfrescoContentNodesChecksumGenerator(
        nodeService: NodeService
    ) =
        RenditionAlfrescoContentNodesChecksumGenerator(nodeService)
}