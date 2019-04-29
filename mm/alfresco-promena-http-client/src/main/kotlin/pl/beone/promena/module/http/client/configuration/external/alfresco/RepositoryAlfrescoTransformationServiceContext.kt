package pl.beone.promena.module.http.client.configuration.external.alfresco

import org.alfresco.service.cmr.dictionary.DictionaryService
import org.alfresco.service.cmr.model.FileFolderService
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.lib.http.client.contract.transformation.TransformationServerService
import pl.beone.promena.module.http.client.external.alfresco.AlfrescoFileDataConverter
import pl.beone.promena.module.http.client.external.alfresco.RepositoryAlfrescoTransformationService

@Configuration
class RepositoryAlfrescoTransformationServiceContext {

    @Bean
    fun repositoryAlfrescoTransformationService(transformationServerService: TransformationServerService,
                                                dataDescriptorConverter: AlfrescoFileDataConverter,
                                                nodeService: NodeService,
                                                fileFolderService: FileFolderService,
                                                dictionaryService: DictionaryService,
                                                namespaceService: NamespaceService,
                                                contentService: ContentService) =
            RepositoryAlfrescoTransformationService(
                    transformationServerService,
                    dataDescriptorConverter,
                    nodeService,
                    fileFolderService,
                    dictionaryService,
                    namespaceService,
                    contentService)
}