package pl.beone.promena.alfresco.module.core.applicationmodel.exception

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class AnotherTransformationIsInProgressException(
    val transformation: Transformation,
    val nodeDescriptors: List<NodeDescriptor>,
    val oldNodesChecksum: String,
    val currentNodesChecksum: String
) : RuntimeException("Couldn't transform <$nodeDescriptors> using <$transformation>. Nodes were changed in the meantime (old checksum <$oldNodesChecksum>, current checksum <$currentNodesChecksum>). Another transformation is in progress...")