package pl.beone.promena.alfresco.module.core.applicationmodel.node

import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus

internal class NodeDescriptorTest {

    companion object {
        private val nodeRef = NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
    }

    @Test
    fun of() {
        NodeDescriptor.of(nodeRef).let {
            it.nodeRef shouldBe nodeRef
            it.metadata shouldBe emptyMetadata()
        }
    }

    @Test
    fun `of with metadata`() {
        val metadata = emptyMetadata() + ("key" to "value")

        NodeDescriptor.of(nodeRef, metadata).let {
            it.nodeRef shouldBe nodeRef
            it.metadata shouldBe metadata
        }
    }
}