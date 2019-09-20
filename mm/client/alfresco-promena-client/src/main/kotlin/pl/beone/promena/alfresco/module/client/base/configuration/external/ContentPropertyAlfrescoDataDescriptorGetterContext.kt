package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.external.ContentPropertyAlfrescoDataDescriptorGetter

@Configuration
class ContentPropertyAlfrescoDataDescriptorGetterContext {

    @Bean
    fun contentPropertyAlfrescoDataDescriptorGetter(
        nodeService: NodeService,
        contentService: ContentService,
        alfrescoDataConverter: AlfrescoDataConverter
    ) =
        ContentPropertyAlfrescoDataDescriptorGetter(
            nodeService,
            contentService,
            alfrescoDataConverter
        )
}