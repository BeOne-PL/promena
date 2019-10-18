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
        NodeDescriptor.Single.of(nodeRef).let { nodeDescriptor ->
            nodeDescriptor.descriptors shouldHaveSize 1

            nodeDescriptor.descriptors[0].let {
                it.nodeRef shouldBe nodeRef
                it.metadata shouldBe emptyMetadata()
            }
        }
    }

    @Test
    fun `single _ of with metadata`() {
        NodeDescriptor.Single.of(nodeRef, metadata).let { nodeDescriptor ->
            nodeDescriptor.descriptors shouldHaveSize 1

            nodeDescriptor.descriptors[0].let {
                it.nodeRef shouldBe nodeRef
                it.metadata shouldBe metadata
            }
        }
    }

    @Test
    fun `multi _ of`() {
        NodeDescriptor.Multi.of(listOf(NodeDescriptor.Single.of(nodeRef), NodeDescriptor.Single.of(nodeRef, metadata))).let { nodeDescriptor ->
            nodeDescriptor.descriptors shouldHaveSize 2

            nodeDescriptor.descriptors[0].let {
                it.nodeRef shouldBe nodeRef
                it.metadata shouldBe emptyMetadata()
            }
            nodeDescriptor.descriptors[1].let {
                it.nodeRef shouldBe nodeRef
                it.metadata shouldBe metadata
            }
        }
    }
}