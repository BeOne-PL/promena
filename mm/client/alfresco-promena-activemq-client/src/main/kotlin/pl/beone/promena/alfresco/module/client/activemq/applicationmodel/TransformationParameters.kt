package pl.beone.promena.alfresco.module.client.activemq.applicationmodel

import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry

data class TransformationParameters(
    val nodeDescriptors: List<NodeDescriptor>,
    val nodesChecksum: String,
    val retry: Retry,
    val attempt: Long,
    val userName: String
)