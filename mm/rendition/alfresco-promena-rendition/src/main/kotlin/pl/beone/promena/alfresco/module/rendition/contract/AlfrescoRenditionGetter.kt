package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef

interface AlfrescoRenditionGetter {

    fun getRenditions(node: NodeRef): List<ChildAssociationRef>

    fun getRendition(node: NodeRef, renditionName: String): ChildAssociationRef?
}