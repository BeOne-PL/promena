package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.messagebroker.external.ContentPropertyAlfrescoDataDescriptorGetter

@Configuration
class ContentPropertyAlfrescoDataDescriptorGetterContext {

    @Bean
    fun contentPropertyAlfrescoDataDescriptorGetter(nodeService: NodeService,
                                                    contentService: ContentService,
                                                    alfrescoDataConverter: AlfrescoDataConverter) =
            ContentPropertyAlfrescoDataDescriptorGetter(nodeService,
                                                        contentService,
                                                        alfrescoDataConverter)
}