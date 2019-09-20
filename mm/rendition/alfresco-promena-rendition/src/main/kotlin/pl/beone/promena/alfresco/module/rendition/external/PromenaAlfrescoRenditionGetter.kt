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
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel.PROP_RENDITION
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getTransformationNodeName
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class PromenaAlfrescoRenditionGetter(
    private val nodeService: NodeService,
    private val alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter
) : AlfrescoRenditionGetter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getRenditions(node: NodeRef): List<ChildAssociationRef> =
        nodeService.getChildAssocs(node, ASSOC_RENDITION, MATCH_ALL)
            .filter { childAssociationRef -> isRendition(childAssociationRef.childRef) }

    override fun getRendition(node: NodeRef, renditionName: String): ChildAssociationRef? =
        try {
            val nodeNameQName = QName.createQName(
                CONTENT_MODEL_1_0_URI,
                alfrescoPromenaRenditionDefinitionGetter.getByRenditionName(renditionName).getTransformationNodeName()
            )

            nodeService.getChildAssocs(node, ASSOC_RENDITION, nodeNameQName)
                .also { childAssociationRefs -> if (childAssociationRefs.isEmpty()) logger.warn { "There is no <$renditionName> rendition node of <$node>" } }
                .map { childAssociationRef -> childAssociationRef to getPropertyModifiedDate(childAssociationRef.childRef) }
                .maxBy { (_, date) -> getOrMinDate(date) }
                ?.first
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            logger.warn(e) { "There is no <$renditionName> rendition available" }
            null
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't get <$renditionName> rendition node of <$node>" }
            null
        }

    private fun isRendition(nodeRef: NodeRef): Boolean =
        try {
            alfrescoPromenaRenditionDefinitionGetter.getByNodeName(nodeService.getProperty(nodeRef, PROP_NAME) as String)
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
}
