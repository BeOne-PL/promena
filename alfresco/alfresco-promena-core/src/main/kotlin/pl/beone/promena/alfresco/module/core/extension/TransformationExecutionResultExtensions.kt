package pl.beone.promena.alfresco.module.core.extension

import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult

fun TransformationExecutionResult.toPrettyString(): String =
    if (nodeRefs.size == 1) {
        nodeRefs[0].toString()
    } else {
        "[" + nodeRefs.joinToString(", ") + "]"
    }