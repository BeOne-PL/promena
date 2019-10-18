package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.cmr.repository.NodeRef

data class TransformationExecutionResult(
    val nodeRefs: List<NodeRef>
)