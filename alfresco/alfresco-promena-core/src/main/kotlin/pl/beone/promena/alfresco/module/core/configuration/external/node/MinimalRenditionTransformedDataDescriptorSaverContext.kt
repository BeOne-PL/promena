package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.transaction.TransactionService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.core.external.node.MinimalRenditionTransformedDataDescriptorSaver
import java.util.*

@Configuration
class MinimalRenditionTransformedDataDescriptorSaverContext {

    @Bean
    fun minimalRenditionTransformedDataDescriptorSaver(
        @Qualifier("global-properties") properties: Properties,
        nodeService: NodeService,
        contentService: ContentService,
        namespaceService: NamespaceService,
        transactionService: TransactionService,
        dataConverter: DataConverter
    ) =
        MinimalRenditionTransformedDataDescriptorSaver(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.transformation.save-if-zero").toBoolean(),
            nodeService,
            contentService,
            namespaceService,
            transactionService,
            dataConverter
        )
}