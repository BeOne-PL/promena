package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef

interface AlfrescoPromenaRenditionTransformer {

    fun transform(nodeRef: NodeRef, renditionName: String): ChildAssociationRef

    fun transformAsync(nodeRef: NodeRef, renditionName: String)
}