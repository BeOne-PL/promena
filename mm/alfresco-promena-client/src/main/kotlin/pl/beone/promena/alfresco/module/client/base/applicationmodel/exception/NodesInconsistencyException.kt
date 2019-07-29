package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

class NodesInconsistencyException(
    val nodeRefs: List<NodeRef>,
    val oldNodesChecksum: String,
    val currentNodesChecksum: String
) : RuntimeException("Nodes <$nodeRefs> have changed in the meantime (old checksum <$oldNodesChecksum>, current checksum <$currentNodesChecksum>)")