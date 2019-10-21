@file:JvmName("TransformationExecutionResultDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.cmr.repository.NodeRef

fun transformationExecutionResult(nodeRefs: List<NodeRef>): TransformationExecutionResult =
    TransformationExecutionResult.of(nodeRefs)

fun transformationExecutionResult(vararg nodeRefs: NodeRef): TransformationExecutionResult =
    TransformationExecutionResult.of(nodeRefs.toList())