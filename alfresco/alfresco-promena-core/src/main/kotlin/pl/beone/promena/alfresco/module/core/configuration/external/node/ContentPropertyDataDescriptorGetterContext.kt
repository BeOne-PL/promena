package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.external.node.ContentPropertyDataDescriptorGetter

@Configuration
class ContentPropertyDataDescriptorGetterContext {

    @Bean
    fun contentPropertyDataDescriptorGetter(
        nodeService: NodeService,
        contentService: ContentService,
        dataConverter: DataConverter
    ) =
        ContentPropertyDataDescriptorGetter(
            nodeService,
            contentService,
            dataConverter
        )
}