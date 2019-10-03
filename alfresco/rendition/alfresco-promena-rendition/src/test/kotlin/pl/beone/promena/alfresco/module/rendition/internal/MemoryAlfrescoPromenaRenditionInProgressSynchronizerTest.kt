package pl.beone.promena.alfresco.module.rendition.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionInProgressException

class MemoryAlfrescoPromenaRenditionInProgressSynchronizerTest {

    private lateinit var memoryAlfrescoPromenaRenditionInProgressManager: MemoryAlfrescoPromenaRenditionInProgressSynchronizer

    @Before
    fun setUp() {
        memoryAlfrescoPromenaRenditionInProgressManager = MemoryAlfrescoPromenaRenditionInProgressSynchronizer()
    }

    @Test
    fun all() {
        val nodeRef = NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        val renditionName = "doclib"

        val nodeRef2 = NodeRef("workspace:/workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        val renditionName2 = "pdf"

        memoryAlfrescoPromenaRenditionInProgressManager.start(nodeRef, renditionName)

        shouldNotThrow<AlfrescoPromenaRenditionInProgressException> {
            memoryAlfrescoPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }

        memoryAlfrescoPromenaRenditionInProgressManager.start(nodeRef2, renditionName2)
        shouldThrow<AlfrescoPromenaRenditionInProgressException> {
            memoryAlfrescoPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }.message shouldBe "Creating rendition <pdf> of <workspace:/workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f> is in progress..."

        memoryAlfrescoPromenaRenditionInProgressManager.finish(nodeRef2, renditionName2)
        shouldNotThrow<AlfrescoPromenaRenditionInProgressException> {
            memoryAlfrescoPromenaRenditionInProgressManager.isInProgress(nodeRef2, renditionName2)
        }
    }
}