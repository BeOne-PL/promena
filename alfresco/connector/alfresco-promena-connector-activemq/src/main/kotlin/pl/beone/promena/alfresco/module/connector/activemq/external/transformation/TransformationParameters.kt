package pl.beone.promena.alfresco.module.connector.activemq.external.transformation

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

data class TransformationParameters(
    val transformation: Transformation,
    val nodeDescriptor: NodeDescriptor,
    val postTransformationExecutor: PostTransformationExecutor?,
    val retry: Retry,
    val dataDescriptor: DataDescriptor,
    val nodesChecksum: String,
    val attempt: Long,
    val userName: String
)