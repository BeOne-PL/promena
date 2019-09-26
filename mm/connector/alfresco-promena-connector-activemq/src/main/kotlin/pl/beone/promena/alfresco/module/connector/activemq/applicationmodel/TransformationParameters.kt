package pl.beone.promena.alfresco.module.connector.activemq.applicationmodel

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry

data class TransformationParameters(
    val nodeDescriptors: List<NodeDescriptor>,
    val nodesChecksum: String,
    val retry: Retry,
    val attempt: Long,
    val userName: String
)