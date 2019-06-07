package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test

class NodeRefsConverterTest {

    companion object {
        private val nodeRefsConverter = NodeRefsConverter()
    }

    @Test
    fun convert() {
        val nodeRef = "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"

        val nodeRefs = nodeRefsConverter.convert(listOf(nodeRef))

        nodeRefs shouldBe listOf(NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
    }
}