package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.model.ContentModel.PROP_MODIFIED
import org.alfresco.model.ContentModel.PROP_NAME
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.alfresco.service.namespace.RegexQNamePattern.MATCH_ALL
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel.PROP_RENDITION
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionManager
import pl.beone.promena.alfresco.module.rendition.extension.getTransformationNodeName
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class PromenaRenditionCoordinator(
    private val nodeService: NodeService,
    private val promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager,
    private val alfrescoPromenaTransformer: AlfrescoPromenaTransformer,
    private val timeout: Duration
) {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val metadataRenditionProperty = "alf_$PROMENA_MODEL_1_0_PREFIX:${PROP_RENDITION.localName}"
    }

    fun getRenditions(node: NodeRef): List<ChildAssociationRef> =
        nodeService.getChildAssocs(node, ASSOC_RENDITION, MATCH_ALL)
            .filter { childAssociationRef -> isRendition(childAssociationRef.childRef) }

    fun getRendition(node: NodeRef, renditionName: String): ChildAssociationRef? =
        try {
            val nodeNameQName = QName.createQName(
                CONTENT_MODEL_1_0_URI,
                promenaRenditionDefinitionManager.getByRenditionName(renditionName).getTransformationNodeName()
            )

            nodeService.getChildAssocs(node, ASSOC_RENDITION, nodeNameQName)
                .also { childAssociationRefs -> if (childAssociationRefs.isEmpty()) logger.warn { "There is no <$renditionName> rendition node of <$node>" } }
                .map { childAssociationRef -> childAssociationRef to getPropertyModifiedDate(childAssociationRef.childRef) }
                .maxBy { (_, date) -> getOrMinDate(date) }
                ?.first
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)

            null
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't get <$renditionName> rendition node of <$node>" }

            null
        }

    fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef? {
        logger.debug { "Performing <$renditionName> rendition sync transformation of <$nodeRef>..." }

        try {
            alfrescoPromenaTransformer.transform(getTransformation(renditionName), createNodeRefWithMetadataRenditionProperty(nodeRef), timeout)
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)
            return null
        } catch (e: Exception) {
            logger.error(e) { "Couldn't perform <$renditionName> rendition sync transformation of <$nodeRef>" }
            return null
        }

        logger.debug { "Finished performing <$renditionName> rendition sync transformation of <$nodeRef>" }

        return getRendition(nodeRef, renditionName)
    }

    fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        logger.debug { "Performing <$renditionName> rendition async transformation of <$nodeRef>..." }

        try {
            alfrescoPromenaTransformer.transformAsync(getTransformation(renditionName), createNodeRefWithMetadataRenditionProperty(nodeRef))
                .subscribe(
                    { logger.debug { "Finished performing <$renditionName> rendition async transformation of <$nodeRef>" } },
                    { exception -> logger.error(exception) { "Couldn't perform <$renditionName> rendition async transformation of <$nodeRef>" } }
                )
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)
        }
    }

    private fun isRendition(nodeRef: NodeRef): Boolean =
        try {
            promenaRenditionDefinitionManager.getByNodeName(nodeService.getProperty(nodeRef, PROP_NAME) as String)
            nodeService.getProperty(nodeRef, PROP_RENDITION) as Boolean
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            false
        }

    private fun getPropertyModifiedDate(nodeRef: NodeRef): LocalDateTime? =
        (nodeService.getProperty(nodeRef, PROP_MODIFIED) as Date?)?.toLocalDateTime()

    private fun Date.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

    private fun getOrMinDate(date: LocalDateTime?): LocalDateTime =
        date ?: LocalDateTime.MIN

    private fun getTransformation(renditionName: String): Transformation =
        promenaRenditionDefinitionManager.getByRenditionName(renditionName).getTransformation()

    private fun createNodeRefWithMetadataRenditionProperty(nodeRef: NodeRef): List<NodeDescriptor> =
        listOf(
            nodeRef.toNodeDescriptor(emptyMetadata() + (metadataRenditionProperty to true))
        )

    private fun logPromenaNoSuchRenditionDefinitionException(e: PromenaNoSuchRenditionDefinitionException, renditionName: String) {
        logger.warn(e) { "There is no <$renditionName> rendition available" }
    }
}
