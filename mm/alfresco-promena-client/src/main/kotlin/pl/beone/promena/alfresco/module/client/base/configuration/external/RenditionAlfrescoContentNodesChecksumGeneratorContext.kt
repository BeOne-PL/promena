package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.external.RenditionAlfrescoContentNodesChecksumGenerator

@Configuration
class RenditionAlfrescoContentNodesChecksumGeneratorContext {

    @Bean
    fun renditionAlfrescoContentNodesChecksumGenerator(
        nodeService: NodeService
    ) =
        RenditionAlfrescoContentNodesChecksumGenerator(nodeService)
}