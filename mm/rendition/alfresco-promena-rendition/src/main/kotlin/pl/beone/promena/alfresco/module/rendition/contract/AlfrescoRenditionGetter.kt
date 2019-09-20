package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef

interface AlfrescoRenditionGetter {

    fun getRenditions(nodeRef: NodeRef): List<ChildAssociationRef>

    fun getRendition(nodeRef: NodeRef, renditionName: String): ChildAssociationRef?
}