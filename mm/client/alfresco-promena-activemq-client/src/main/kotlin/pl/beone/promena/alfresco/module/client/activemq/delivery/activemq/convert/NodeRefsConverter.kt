package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert

import org.alfresco.service.cmr.repository.NodeRef

class NodeRefsConverter {

    fun convert(nodeRefs: List<String>): List<NodeRef> =
        nodeRefs.map { NodeRef(it) }
}