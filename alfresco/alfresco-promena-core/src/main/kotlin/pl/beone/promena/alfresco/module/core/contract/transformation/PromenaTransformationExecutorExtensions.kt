package pl.beone.promena.alfresco.module.core.contract.transformation

import org.alfresco.service.ServiceRegistry
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.PostTransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.transformer.contract.transformation.Transformation

typealias PostTransformationExecutionAlias = (transformation: Transformation, nodeDescriptor: NodeDescriptor, serviceRegistry: ServiceRegistry, result: TransformationExecutionResult) -> Unit

fun PromenaTransformationExecutor.execute(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    postTransformationExecution: PostTransformationExecutionAlias?,
    retry: Retry?
) = execute(
    transformation,
    nodeDescriptor,
    postTransformationExecution?.let {
        PostTransformationExecution { transformation, nodeDescriptor, serviceRegistry, result ->
            postTransformationExecution(transformation, nodeDescriptor, serviceRegistry, result)
        }
    },
    retry
)

fun PromenaTransformationExecutor.execute(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor
) = execute(transformation, nodeDescriptor, null, null)

fun PromenaTransformationExecutor.execute(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    retry: Retry?
) = execute(transformation, nodeDescriptor, null, retry)

fun PromenaTransformationExecutor.execute(
    transformation: Transformation,
    nodeDescriptor: NodeDescriptor,
    postTransformationExecution: PostTransformationExecutionAlias?
) = execute(
    transformation,
    nodeDescriptor,
    postTransformationExecution?.let {
        PostTransformationExecution { serviceRegistry, transformation, nodeDescriptor, result ->
            postTransformationExecution(serviceRegistry, transformation, nodeDescriptor, result)
        }
    },
    null
)
