package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.NodeRef

interface PromenaRenditionInProgressSynchronizer {

    fun start(nodeRef: NodeRef, renditionName: String)

    fun finish(nodeRef: NodeRef, renditionName: String)

    fun isInProgress(nodeRef: NodeRef, renditionName: String)
}