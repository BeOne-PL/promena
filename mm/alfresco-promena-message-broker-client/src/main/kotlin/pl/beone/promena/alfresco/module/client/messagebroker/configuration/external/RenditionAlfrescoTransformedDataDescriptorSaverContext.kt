package pl.beone.promena.alfresco.module.client.messagebroker.configuration.external

import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.transaction.TransactionService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.getRequiredProperty
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.messagebroker.external.RenditionAlfrescoTransformedDataDescriptorSaver
import java.util.*

@Configuration
class RenditionAlfrescoTransformedDataDescriptorSaverContext {

    @Bean
    fun renditionAlfrescoTransformedDataDescriptorSaver(@Qualifier("global-properties") properties: Properties,
                                                        nodeService: NodeService,
                                                        contentService: ContentService,
                                                        namespaceService: NamespaceService,
                                                        transactionService: TransactionService,
                                                        alfrescoDataConverter: AlfrescoDataConverter) =
            RenditionAlfrescoTransformedDataDescriptorSaver(properties.getRequiredProperty("promena.transformation.saveIfZero").toBoolean(),
                                                            nodeService,
                                                            contentService,
                                                            namespaceService,
                                                            transactionService,
                                                            alfrescoDataConverter)
}