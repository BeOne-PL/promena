package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.cmr.repository.NodeRef

data class TransformationExecutionResult internal constructor(
    val nodeRefs: List<NodeRef>
) {

    companion object {
        @JvmStatic
        fun of(nodeRefs: List<NodeRef>): TransformationExecutionResult =
            TransformationExecutionResult(nodeRefs)

        @JvmStatic
        fun of(vararg nodeRefs: NodeRef): TransformationExecutionResult =
            TransformationExecutionResult(nodeRefs.toList())
    }
}