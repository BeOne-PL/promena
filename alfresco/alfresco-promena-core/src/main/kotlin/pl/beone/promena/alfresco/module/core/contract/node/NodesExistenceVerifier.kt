package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef

interface NodesExistenceVerifier {

    @Throws(InvalidNodeRefException::class)
    fun verify(nodeRefs: List<NodeRef>)
}