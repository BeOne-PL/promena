package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.NodeRef

interface NodesExistenceVerifier {

    fun verify(nodeRefs: List<NodeRef>)
}