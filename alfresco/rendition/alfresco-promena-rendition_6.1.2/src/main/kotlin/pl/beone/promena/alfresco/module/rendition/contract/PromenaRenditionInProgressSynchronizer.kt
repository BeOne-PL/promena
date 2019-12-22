package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution

interface PromenaRenditionInProgressSynchronizer {

    fun start(nodeRef: NodeRef, renditionName: String, transformationExecution: TransformationExecution)

    fun finish(nodeRef: NodeRef, renditionName: String)

    fun isInProgress(nodeRef: NodeRef, renditionName: String)
}