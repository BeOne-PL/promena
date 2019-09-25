@file:JvmName("NodeDescriptorDslDataDescriptorDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.node

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

fun noMetadataNodeDescriptor(nodeRef: NodeRef): NodeDescriptor =
    NodeDescriptor.of(nodeRef)

fun nodeDescriptor(nodeRef: NodeRef, metadata: Metadata): NodeDescriptor =
    NodeDescriptor.of(nodeRef, metadata)

fun List<NodeRef>.toNodeDescriptors(): List<NodeDescriptor> =
    this.map(::noMetadataNodeDescriptor)

fun NodeRef.toNodeDescriptor(metadata: Metadata = emptyMetadata()): NodeDescriptor =
    NodeDescriptor.of(this, metadata)

fun List<NodeDescriptor>.toNodeRefs(): List<NodeRef> =
    this.map(NodeDescriptor::nodeRef)