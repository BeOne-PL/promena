package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef

interface AlfrescoNodesChecksumGenerator {

    fun generateChecksum(nodeRefs: List<NodeRef>): String
}