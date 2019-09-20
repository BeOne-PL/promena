package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.model.ContentModel.PROP_MODIFIED
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.RegexQNamePattern.MATCH_ALL
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class PromenaAlfrescoRenditionGetter(
    private val nodeService: NodeService
) : AlfrescoRenditionGetter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getRenditions(nodeRef: NodeRef): List<ChildAssociationRef> =
        nodeService.getChildAssocs(nodeRef, ASSOC_RENDITION, MATCH_ALL)
            .filter { childAssociationRef -> isRendition(childAssociationRef.childRef) }

    override fun getRendition(nodeRef: NodeRef, renditionName: String): ChildAssociationRef? =
        try {
            nodeService.getChildAssocsByPropertyValue(nodeRef, PROP_RENDITION_NAME, renditionName)
                .map { childAssociationRef -> childAssociationRef to getPropertyModifiedDate(childAssociationRef.childRef) }
                .maxBy { (_, date) -> getOrMinDate(date) }
                ?.first
        } catch (e: Exception) {
            logger.warn(e) { "Couldn't get <$renditionName> rendition node of <$nodeRef>" }
            throw e
        }

    private fun isRendition(nodeRef: NodeRef): Boolean =
        nodeService.getProperty(nodeRef, PROP_RENDITION_NAME) != null

    private fun getPropertyModifiedDate(nodeRef: NodeRef): LocalDateTime? =
        (nodeService.getProperty(nodeRef, PROP_MODIFIED) as Date?)?.toLocalDateTime()

    private fun Date.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

    private fun getOrMinDate(date: LocalDateTime?): LocalDateTime =
        date ?: LocalDateTime.MIN
}
