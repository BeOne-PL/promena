package pl.beone.promena.alfresco.module.core.extension

import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult

fun TransformationExecutionResult.toPrettyString(): String =
    "[" + nodeRefs.joinToString(", ") + "]"