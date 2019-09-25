package pl.beone.promena.alfresco.module.core.applicationmodel.node

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

data class NodeDescriptor internal constructor(
    val nodeRef: NodeRef,
    val metadata: Metadata
) {

    companion object {
        @JvmStatic
        fun of(nodeRef: NodeRef, metadata: Metadata): NodeDescriptor =
            NodeDescriptor(nodeRef, metadata)

        @JvmStatic
        fun of(nodeRef: NodeRef): NodeDescriptor =
            NodeDescriptor(nodeRef, emptyMetadata())
    }
}