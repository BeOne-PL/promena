package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.matchers.string.shouldHaveMinLength
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.rad.test.AlfrescoTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants

@RunWith(AlfrescoTestRunner::class)
class RenditionAlfrescoContentNodesChecksumGeneratorTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun generateChecksum() {
        val nodeRefs = (0..1).map { index ->
            createOrGetIntegrationTestsFolder().createNode().apply {
                saveContent(MediaTypeConstants.TEXT_PLAIN, index.toString())
            }
        }

        RenditionAlfrescoContentNodesChecksumGenerator(serviceRegistry.nodeService)
                .generateChecksum(nodeRefs) shouldHaveMinLength 2
    }

    @Test
    fun generateChecksum_shouldThrowIllegalArgumentException() {
        shouldThrow<IllegalArgumentException> {
            RenditionAlfrescoContentNodesChecksumGenerator(serviceRegistry.nodeService).generateChecksum(emptyList())
        }.message shouldBe "You have to pass at least one node"
    }
}