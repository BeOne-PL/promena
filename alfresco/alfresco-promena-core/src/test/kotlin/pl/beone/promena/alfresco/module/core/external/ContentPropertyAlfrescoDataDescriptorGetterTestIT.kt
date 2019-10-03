package pl.beone.promena.alfresco.module.core.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.util.*

@RunWith(AlfrescoTestRunner::class)
class ContentPropertyAlfrescoDataDescriptorGetterTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun get_shouldDetermineDataDescriptor() {
        val data = "test".toMemoryData()
        val mediaType = TEXT_PLAIN
        val metadata = emptyMetadata() + ("key" to "value")
        val node = with(createOrGetIntegrationTestsFolder()) {
            createNode().apply { saveContent(mediaType, "no matter") }
        }

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { createData(any()) } returns data
        }

        ContentPropertyAlfrescoDataDescriptorGetter(serviceRegistry.nodeService, serviceRegistry.contentService, alfrescoDataConverter)
            .get(listOf(node.toNodeDescriptor(metadata))).let {
                it shouldBe singleDataDescriptor(data, mediaType, metadata)
            }
    }

    @Test
    fun get_shouldThrowNodeDoesNotExist() {
        shouldThrow<NodeDoesNotExist> {
            ContentPropertyAlfrescoDataDescriptorGetter(serviceRegistry.nodeService, serviceRegistry.contentService, mockk())
                .get(listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}").toNodeDescriptor()))
        }
    }
}