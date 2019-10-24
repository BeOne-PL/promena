package pl.beone.promena.alfresco.module.core.contract.transformation

import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import java.time.Duration

interface PromenaTransformationManager {

    fun getResult(transformationExecution: TransformationExecution, waitMax: Duration? = null): TransformationExecutionResult

    interface PromenaMutableTransformationManager : PromenaTransformationManager {

        fun startTransformation(): TransformationExecution

        fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult)

        fun completeErrorTransformation(transformationExecution: TransformationExecution, throwable: Throwable)
    }
}