package pl.beone.promena.alfresco.module.core.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.transaction.TransactionService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.external.MinimalRenditionAlfrescoTransformedDataDescriptorSaver
import java.util.*

@Configuration
class MinimalRenditionAlfrescoTransformedDataDescriptorSaverContext {

    @Bean
    fun minimalRenditionAlfrescoTransformedDataDescriptorSaver(
        @Qualifier("global-properties") properties: Properties,
        nodeService: NodeService,
        contentService: ContentService,
        namespaceService: NamespaceService,
        transactionService: TransactionService,
        alfrescoDataConverter: AlfrescoDataConverter
    ) =
        MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.save-if-zero").toBoolean(),
            nodeService,
            contentService,
            namespaceService,
            transactionService,
            alfrescoDataConverter
        )
}