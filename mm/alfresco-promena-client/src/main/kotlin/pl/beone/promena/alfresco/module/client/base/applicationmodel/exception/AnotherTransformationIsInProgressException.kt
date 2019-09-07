package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.transformation.Transformation

class AnotherTransformationIsInProgressException(
    val transformation: Transformation,
    val nodeRefs: List<NodeRef>,
    val oldNodesChecksum: String,
    val currentNodesChecksum: String
) : RuntimeException("Couldn't perform <$transformation> on nodes <$nodeRefs>. Nodes were changed in the meantime (old checksum <$oldNodesChecksum>, current checksum <$currentNodesChecksum>). Another transformation is in progress...")