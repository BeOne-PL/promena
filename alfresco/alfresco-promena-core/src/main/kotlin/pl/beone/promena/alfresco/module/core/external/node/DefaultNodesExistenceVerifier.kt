package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.contract.node.NodesExistenceVerifier

class DefaultNodesExistenceVerifier(
    private val serviceRegistry: ServiceRegistry
) : NodesExistenceVerifier {

    override fun verify(nodeRefs: List<NodeRef>) {
        nodeRefs.forEach(::throwIfDoesNotExist)
    }

    private fun throwIfDoesNotExist(nodeRef: NodeRef) {
        if (!serviceRegistry.nodeService.exists(nodeRef)) {
            throw InvalidNodeRefException("Node <$nodeRef> doesn't exist", nodeRef)
        }
    }
}