package pl.beone.promena.alfresco.module.rendition.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaRenditionInProgressException

class MemoryPromenaRenditionInProgressSynchronizerTest {

    private lateinit var memoryPromenaRenditionInProgressManager: MemoryPromenaRenditionInProgressSynchronizer

    @Before
    fun setUp() {
        memoryPromenaRenditionInProgressManager = MemoryPromenaRenditionInProgressSynchronizer()
    }

    @Test
    fun all() {
        val nodeRef = NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        val renditionName = "doclib"

        val nodeRef2 = NodeRef("workspace:/workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        val renditionName2 = "pdf"

        memoryPromenaRenditionInProgressManager.start(nodeRef, renditionName)

        shouldNotThrow<PromenaRenditionInProgressException> {
            memoryPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }

        memoryPromenaRenditionInProgressManager.start(nodeRef2, renditionName2)
        shouldThrow<PromenaRenditionInProgressException> {
            memoryPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }.message shouldBe "Creating rendition <pdf> of <workspace:/workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f> is in progress..."

        memoryPromenaRenditionInProgressManager.finish(nodeRef2, renditionName2)
        shouldNotThrow<PromenaRenditionInProgressException> {
            memoryPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }
    }
}