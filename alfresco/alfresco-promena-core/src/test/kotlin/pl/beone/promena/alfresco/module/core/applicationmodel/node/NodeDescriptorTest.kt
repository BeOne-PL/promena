package pl.beone.promena.alfresco.module.core.applicationmodel.node

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

internal class NodeDescriptorTest {

    companion object {
        private val nodeRef = mockk<NodeRef>()
        private val metadata = mockk<Metadata>()
    }

    @Test
    fun `single _ of`() {
        with(NodeDescriptor.Single.of(nodeRef)) {
            descriptors shouldHaveSize 1

            with(descriptors[0]) {
                nodeRef shouldBe nodeRef
                metadata shouldBe emptyMetadata()
            }
        }
    }

    @Test
    fun `single _ of with metadata`() {
        with(NodeDescriptor.Single.of(nodeRef, metadata)) {
            descriptors shouldHaveSize 1

            with(descriptors[0]) {
                nodeRef shouldBe nodeRef
                metadata shouldBe metadata
            }
        }
    }

    @Test
    fun `multi _ of`() {
        with(NodeDescriptor.Multi.of(listOf(NodeDescriptor.Single.of(nodeRef), NodeDescriptor.Single.of(nodeRef, metadata)))) {
            descriptors shouldHaveSize 2

            with(descriptors[0]) {
                nodeRef shouldBe nodeRef
                metadata shouldBe emptyMetadata()
            }
            with(descriptors[1]) {
                nodeRef shouldBe nodeRef
                metadata shouldBe metadata
            }
        }
    }
}