package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.NodeRef

interface NodesChecksumGenerator {

    fun generate(nodeRefs: List<NodeRef>): String
}