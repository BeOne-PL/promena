package pl.beone.promena.alfresco.module.rendition.internal

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaRenditionInProgressException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionInProgressSynchronizer

class MemoryPromenaRenditionInProgressSynchronizer : PromenaRenditionInProgressSynchronizer {

    private data class RenditionKey(
        val nodeRef: NodeRef,
        val renditionName: String
    )

    private val renditions = HashMap<RenditionKey, TransformationExecution>()

    @Synchronized
    override fun start(nodeRef: NodeRef, renditionName: String, transformationExecution: TransformationExecution) {
        renditions[RenditionKey(nodeRef, renditionName)] = transformationExecution
    }

    @Synchronized
    override fun finish(nodeRef: NodeRef, renditionName: String) {
        renditions.remove(RenditionKey(nodeRef, renditionName))
    }

    @Synchronized
    override fun isInProgress(nodeRef: NodeRef, renditionName: String) {
        val transformationExecution = renditions[RenditionKey(nodeRef, renditionName)]
        if (transformationExecution != null) {
            throw PromenaRenditionInProgressException(nodeRef, renditionName, transformationExecution)
        }
    }
}