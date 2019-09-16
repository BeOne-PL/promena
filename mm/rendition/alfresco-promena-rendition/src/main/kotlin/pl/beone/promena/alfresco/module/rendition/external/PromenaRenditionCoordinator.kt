package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.alfresco.service.namespace.RegexQNamePattern
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.base.util.createNodeName
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import java.time.Duration
import java.time.LocalDateTime

class PromenaRenditionCoordinator(
    private val nodeService: NodeService,
    private val promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager,
    private val alfrescoPromenaTransformer: AlfrescoPromenaTransformer,
    private val timeout: Duration
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getRenditions(node: NodeRef): List<ChildAssociationRef> =
        nodeService.getChildAssocs(node, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL)
            .map { childRef -> childRef to nodeService.getProperty(childRef.childRef, ContentModel.PROP_NAME) as String }
            .filter { (_, nodeName) -> hasRendition(nodeName) }
            .map { (childRef) -> childRef }

    fun getRendition(node: NodeRef, renditionName: String): ChildAssociationRef? =
        try {
            val nodeNameQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                promenaRenditionDefinitionManager.getByRenditionName(renditionName).getTransformation().createNodeName()
            )

            nodeService.getChildAssocs(node, RenditionModel.ASSOC_RENDITION, nodeNameQName)
                .also { childRefs -> if (childRefs.isEmpty()) logger.warn { "There is no rendition <$renditionName> node of <$node>" } }
                .map { childRef -> childRef to nodeService.getProperty(childRef.childRef, ContentModel.PROP_MODIFIED) as LocalDateTime }
                .maxBy { (_, date) -> date }
                ?.first
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)

            null
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't get rendition <$renditionName> node of <$node>" }

            null
        }

    fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef? {
        logger.debug { "Performing rendition <$renditionName> transformation of <$nodeRef>..." }

        try {
            alfrescoPromenaTransformer.transform(
                promenaRenditionDefinitionManager.getByRenditionName(renditionName).getTransformation(),
                listOf(nodeRef),
                timeout
            )
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)

            return null
        } catch (e: Exception) {
            logger.error(e) { "Couldn't perform rendition <$renditionName> transformation of <$nodeRef>" }

            return null
        }

        logger.debug { "Finished performing rendition <$renditionName> transformation of <$nodeRef>" }

        return getRendition(nodeRef, renditionName)
    }

    fun transformAsync(nodeRef: NodeRef, renditionName: String) {
        logger.debug { "Performing rendition <$renditionName> transformation of <$nodeRef>..." }

        try {
            alfrescoPromenaTransformer.transformAsync(
                promenaRenditionDefinitionManager.getByRenditionName(renditionName).getTransformation(),
                listOf(nodeRef)
            )
                .also { logger.debug { "Finished performing rendition <$renditionName> transformation of <$nodeRef>" } }
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logPromenaNoSuchRenditionDefinitionException(e, renditionName)
        }
    }

    private fun hasRendition(nodeName: String): Boolean =
        try {
            promenaRenditionDefinitionManager.getByNodeName(nodeName)
            true
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            false
        }

    private fun logPromenaNoSuchRenditionDefinitionException(e: PromenaNoSuchRenditionDefinitionException, renditionName: String) {
        logger.warn(e) { "There is no <$renditionName> rendition available" }
    }
}