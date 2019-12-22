package pl.beone.promena.alfresco.module.rendition.external

import org.alfresco.model.ContentModel.PROP_CREATED
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.RegexQNamePattern.MATCH_ALL
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_RENDITION_NAME
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class PromenaRenditionGetter(
    private val nodeService: NodeService
) : RenditionGetter {

    override fun getRenditions(nodeRef: NodeRef): List<ChildAssociationRef> =
        nodeService.getChildAssocs(nodeRef, ASSOC_RENDITION, MATCH_ALL)
            .filter { childAssociationRef -> isRendition(childAssociationRef.childRef) }

    override fun getRendition(nodeRef: NodeRef, renditionName: String): ChildAssociationRef? =
        nodeService.getChildAssocsByPropertyValue(nodeRef, PROPERTY_RENDITION_NAME, renditionName)
            .map { childAssociationRef -> childAssociationRef to getPropertyModifiedDate(childAssociationRef.childRef) }
            .maxBy { (_, date) -> date.orMinDate() }
            ?.first

    private fun isRendition(nodeRef: NodeRef): Boolean =
        nodeService.getProperty(nodeRef, PROPERTY_RENDITION_NAME) != null

    private fun getPropertyModifiedDate(nodeRef: NodeRef): LocalDateTime? =
        (nodeService.getProperty(nodeRef, PROP_CREATED) as Date?)?.toLocalDateTime()

    private fun Date.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

    private fun LocalDateTime?.orMinDate(): LocalDateTime =
        this ?: LocalDateTime.MIN
}
