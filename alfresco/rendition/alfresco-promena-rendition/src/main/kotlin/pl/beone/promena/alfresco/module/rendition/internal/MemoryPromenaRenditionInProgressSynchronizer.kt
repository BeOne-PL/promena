package pl.beone.promena.alfresco.module.rendition.internal

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaRenditionInProgressException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer

class MemoryPromenaRenditionInProgressSynchronizer : PromenaRenditionInProgressSynchronizer {

    private data class RenditionDescription(
        val nodeRef: NodeRef,
        val renditionName: String
    )

    private val renditionDescriptions = ArrayList<RenditionDescription>()

    @Synchronized
    override fun start(nodeRef: NodeRef, renditionName: String) {
        renditionDescriptions.add(RenditionDescription(nodeRef, renditionName))
    }

    @Synchronized
    override fun finish(nodeRef: NodeRef, renditionName: String) {
        renditionDescriptions.remove(RenditionDescription(nodeRef, renditionName))
    }

    @Synchronized
    override fun isInProgress(nodeRef: NodeRef, renditionName: String) {
        if (renditionDescriptions.contains(RenditionDescription(nodeRef, renditionName))) {
            throw PromenaRenditionInProgressException(nodeRef, renditionName)
        }
    }
}