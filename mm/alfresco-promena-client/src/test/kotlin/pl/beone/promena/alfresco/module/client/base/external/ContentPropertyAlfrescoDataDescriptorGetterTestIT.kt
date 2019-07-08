package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.util.*

@RunWith(AlfrescoTestRunner::class)
class ContentPropertyAlfrescoDataDescriptorGetterTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun get_shouldDetermineDataDescriptor() {
        val data = InMemoryData("test".toByteArray())
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val node = with(createOrGetIntegrationTestsFolder()) {
            createNode().apply { saveContent(mediaType, "no matter") }
        }

        val alfrescoDataConverter = mockk<AlfrescoDataConverter> {
            every { createData(any()) } returns data
        }

        ContentPropertyAlfrescoDataDescriptorGetter(serviceRegistry.nodeService, serviceRegistry.contentService, alfrescoDataConverter)
                .get(listOf(node)).let {
                    it shouldBe listOf(DataDescriptor(data, mediaType, MapMetadata.empty()))
                }
    }

    @Test
    fun get_shouldThrowNodeDoesNotExist() {
        shouldThrow<NodeDoesNotExist> {
            ContentPropertyAlfrescoDataDescriptorGetter(serviceRegistry.nodeService, serviceRegistry.contentService, mockk())
                    .get(listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}")))
        }
    }
}