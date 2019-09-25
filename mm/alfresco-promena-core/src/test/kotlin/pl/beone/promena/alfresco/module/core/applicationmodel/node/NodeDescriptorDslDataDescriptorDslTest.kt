package pl.beone.promena.alfresco.module.core.applicationmodel.node

import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus

class NodeDescriptorDslDataDescriptorDslTest {

    companion object {
        private val nodeRef = NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        private val nodeRef2 = NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133")
        private val metadata = emptyMetadata() + ("key" to "value")
    }

    @Test
    fun noMetadataNodeDescriptor() {
        noMetadataNodeDescriptor(nodeRef) shouldBe
                NodeDescriptor.of(nodeRef, emptyMetadata())
    }

    @Test
    fun nodeDescriptor() {
        nodeDescriptor(nodeRef, metadata) shouldBe
                NodeDescriptor.of(nodeRef, metadata)
    }

    @Test
    fun toNodeDescriptors() {
        listOf(nodeRef, nodeRef2).toNodeDescriptors() shouldBe
                listOf(
                    NodeDescriptor.of(nodeRef),
                    NodeDescriptor.of(nodeRef2)
                )
    }

    @Test
    fun toNodeDescriptor() {
        nodeRef.toNodeDescriptor(metadata) shouldBe
                NodeDescriptor.of(nodeRef, metadata)
    }

    @Test
    fun toNodeRefs() {
        listOf(
            NodeDescriptor(nodeRef, metadata),
            NodeDescriptor(nodeRef2, emptyMetadata())
        ).toNodeRefs() shouldBe
                listOf(nodeRef, nodeRef2)
    }
}