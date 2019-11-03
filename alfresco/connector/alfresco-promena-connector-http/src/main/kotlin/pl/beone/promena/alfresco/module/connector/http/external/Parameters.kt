package pl.beone.promena.alfresco.module.connector.http.external

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

internal data class Parameters(
    val transformation: Transformation,
    val nodeDescriptor: NodeDescriptor,
    val postTransformationExecutor: PostTransformationExecutor?,
    val retry: Retry,
    val dataDescriptor: DataDescriptor,
    val transformationExecution: TransformationExecution,
    val nodesChecksum: String,
    val userName: String
)