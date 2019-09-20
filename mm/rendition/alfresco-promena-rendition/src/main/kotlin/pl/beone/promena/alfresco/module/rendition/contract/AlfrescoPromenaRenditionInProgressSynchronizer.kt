package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionInProgressException

interface AlfrescoPromenaRenditionInProgressSynchronizer {

    fun start(nodeRef: NodeRef, renditionName: String)

    fun finish(nodeRef: NodeRef, renditionName: String)

    @Throws(AlfrescoPromenaRenditionInProgressException::class)
    fun isInProgress(nodeRef: NodeRef, renditionName: String)
}