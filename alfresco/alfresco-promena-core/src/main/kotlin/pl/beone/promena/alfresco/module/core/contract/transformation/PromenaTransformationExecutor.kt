package pl.beone.promena.alfresco.module.core.contract.transformation

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.PostTransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.transformer.contract.transformation.Transformation

interface PromenaTransformationExecutor {

    fun execute(
        transformation: Transformation,
        nodeDescriptor: NodeDescriptor,
        postTransformationExecution: PostTransformationExecution? = null,
        retry: Retry? = null
    ): TransformationExecution
}