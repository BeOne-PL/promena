@file:JvmName("TransformationExecutionDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

fun transformationExecution(id: String): TransformationExecution =
    TransformationExecution.of(id)