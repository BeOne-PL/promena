package pl.beone.promena.alfresco.module.core.applicationmodel.node

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

class NodeDescriptorDslTest {

    companion object {
        private val nodeRef = mockk<NodeRef>()
        private val metadata = mockk<Metadata>()

        private val nodeRef2 = mockk<NodeRef>()
        private val metadata2 = mockk<Metadata>()

        private val singleNodeDescriptor = NodeDescriptor.Single.of(nodeRef, metadata)
        private val singleNodeDescriptor2 = NodeDescriptor.Single.of(nodeRef2, metadata2)
    }

    @Test
    fun singleNodeDescriptor() {
        singleNodeDescriptor(nodeRef, metadata) shouldBe
                singleNodeDescriptor
    }

    @Test
    fun toSingleNodeDescriptor() {
        nodeRef.toSingleNodeDescriptor() shouldBe
                singleNodeDescriptor(nodeRef, emptyMetadata())

        nodeRef.toSingleNodeDescriptor(metadata) shouldBe
                singleNodeDescriptor(nodeRef, metadata)
    }

    @Test
    fun `plus _ single node descriptor`() {
        singleNodeDescriptor + singleNodeDescriptor2 shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2))
    }

    @Test
    fun multiNodeDescriptor() {
        multiNodeDescriptor(singleNodeDescriptor, listOf(singleNodeDescriptor2)) shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2))

        multiNodeDescriptor(singleNodeDescriptor, singleNodeDescriptor2) shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2))
    }

    @Test
    fun `plus _ multi + single data descriptor`() {
        multiNodeDescriptor(singleNodeDescriptor, singleNodeDescriptor2) + singleNodeDescriptor shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2, singleNodeDescriptor))
    }

    @Test
    fun `plus _ multi + multi data descriptor`() {
        val multiNodeDescriptor = multiNodeDescriptor(singleNodeDescriptor, singleNodeDescriptor2)
        multiNodeDescriptor + multiNodeDescriptor shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2, singleNodeDescriptor, singleNodeDescriptor2))
    }

    @Test
    fun `nodeDescriptor _ zero data descriptors - Empty`() {
        shouldThrow<IllegalArgumentException> {
            nodeDescriptor()
        }.message shouldBe "NodeDescriptor must consist of at least one descriptor"
    }

    @Test
    fun `nodeDescriptor _ one single data descriptors - Single`() {
        nodeDescriptor(singleNodeDescriptor) shouldBe
                singleNodeDescriptor
    }

    @Test
    fun `nodeDescriptor _ two single data descriptors - Multi`() {
        nodeDescriptor(listOf(singleNodeDescriptor, singleNodeDescriptor2)) shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2))

        nodeDescriptor(singleNodeDescriptor, singleNodeDescriptor2) shouldBe
                NodeDescriptor.Multi.of(listOf(singleNodeDescriptor, singleNodeDescriptor2))
    }

    @Test
    fun toNodeDescriptor() {
        listOf(singleNodeDescriptor).toNodeDescriptor() shouldBe
                singleNodeDescriptor
    }

    @Test
    fun toNodeRefs() {
        singleNodeDescriptor.toNodeRefs() shouldBe
                listOf(nodeRef)
    }
}