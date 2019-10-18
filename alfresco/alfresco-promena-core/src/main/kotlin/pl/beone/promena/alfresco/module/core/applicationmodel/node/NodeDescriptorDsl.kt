@file:JvmName("NodeDescriptorDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.node

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

fun singleNodeDescriptor(nodeRef: NodeRef, metadata: Metadata = emptyMetadata()): NodeDescriptor.Single =
    NodeDescriptor.Single.of(nodeRef, metadata)

fun NodeRef.toSingleNodeDescriptor(metadata: Metadata = emptyMetadata()): NodeDescriptor.Single =
    NodeDescriptor.Single.of(this, metadata)

operator fun NodeDescriptor.Single.plus(descriptor: NodeDescriptor.Single): NodeDescriptor.Multi =
    NodeDescriptor.Multi.of(descriptors + descriptor)

fun multiNodeDescriptor(descriptor: NodeDescriptor.Single, descriptors: List<NodeDescriptor.Single>): NodeDescriptor.Multi =
    NodeDescriptor.Multi.of(listOf(descriptor) + descriptors)

fun multiNodeDescriptor(descriptor: NodeDescriptor.Single, vararg descriptors: NodeDescriptor.Single): NodeDescriptor.Multi =
    multiNodeDescriptor(descriptor, descriptors.toList())

operator fun NodeDescriptor.Multi.plus(descriptor: NodeDescriptor.Single): NodeDescriptor.Multi =
    NodeDescriptor.Multi.of(descriptors + descriptor)

operator fun NodeDescriptor.Multi.plus(descriptor: NodeDescriptor.Multi): NodeDescriptor.Multi =
    NodeDescriptor.Multi.of(descriptors + descriptor.descriptors)

fun nodeDescriptor(descriptors: List<NodeDescriptor.Single>): NodeDescriptor =
    when (descriptors.size) {
        0 -> throw IllegalArgumentException("NodeDescriptor must consist of at least one descriptor")
        1 -> descriptors.first()
        else -> NodeDescriptor.Multi.of(descriptors.toList())
    }

fun nodeDescriptor(vararg descriptors: NodeDescriptor.Single): NodeDescriptor =
    nodeDescriptor(descriptors.toList())

fun List<NodeDescriptor.Single>.toNodeDescriptor(): NodeDescriptor =
    nodeDescriptor(this)

fun NodeDescriptor.toNodeRefs(): List<NodeRef> =
    this.descriptors.map(NodeDescriptor.Single::nodeRef)