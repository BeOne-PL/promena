package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.NodeRef

interface NodesChecksumGenerator {

    fun generateChecksum(nodeRefs: List<NodeRef>): String
}