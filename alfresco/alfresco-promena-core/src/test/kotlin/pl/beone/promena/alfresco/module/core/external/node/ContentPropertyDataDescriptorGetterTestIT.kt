package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.shouldThrowExactly
import io.mockk.every
import io.mockk.mockk
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.util.*

@RunWith(AlfrescoTestRunner::class)
class ContentPropertyDataDescriptorGetterTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun get_shouldDetermineDataDescriptor() {
        val data = "test".toMemoryData()
        val mediaType = TEXT_PLAIN
        val metadata = emptyMetadata() + ("key" to "value")
        val nodeRef = with(createOrGetIntegrationTestsFolder()) {
            createNode().apply { saveContent(mediaType, "no matter") }
        }

        val dataConverter = mockk<DataConverter> {
            every { createData(any()) } returns data
        }

        ContentPropertyDataDescriptorGetter(dataConverter, serviceRegistry)
            .get(nodeRef.toSingleNodeDescriptor(metadata)) shouldBe singleDataDescriptor(data, mediaType, metadata)
    }

    @Test
    fun get_shouldThrowIllegalStateException() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()

        shouldThrowExactly<IllegalStateException> {
            ContentPropertyDataDescriptorGetter(mockk(), serviceRegistry)
                .get(nodeRef.toSingleNodeDescriptor())
        }.message shouldBe "Node <$nodeRef> has no content"
    }

    @Test
    fun get_shouldThrowNodeDoesNotExist() {
        shouldThrow<InvalidNodeRefException> {
            ContentPropertyDataDescriptorGetter(mockk(), serviceRegistry)
                .get(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}").toSingleNodeDescriptor())
        }
    }
}