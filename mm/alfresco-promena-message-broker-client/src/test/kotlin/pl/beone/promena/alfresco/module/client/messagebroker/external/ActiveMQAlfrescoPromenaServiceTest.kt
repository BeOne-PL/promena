package pl.beone.promena.alfresco.module.client.messagebroker.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.time.Duration

class ActiveMQAlfrescoPromenaServiceTest {

    companion object {
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"),
                                      NodeRef("workspace://SpacesStore/a36d5c1a-e32c-478b-ad8b-14b2882115d1"))
        private val dataDescriptors = listOf(DataDescriptor(InMemoryData("test".toByteArray()), TEXT_PLAIN))
        private const val transformerId = "transformer-test"
        private val targetMediaType = APPLICATION_PDF
        private val resultNodeRefs = listOf(NodeRef("workspace://SpacesStore/c0b95525-26a6-4067-9756-6bec11c93c70"),
                                            NodeRef("workspace://SpacesStore/6967ef69-2768-411e-8ab4-4dc66001911b"))
    }

    @Test
    fun transform() {
        val parameters = MapParameters(mapOf("key" to "value"))
        val duration = Duration.ofSeconds(5)

        val completedTransformationManager = mockk<CompletedTransformationManager> {
            every { getTransformedNodeRefs(any(), duration) } returns resultNodeRefs

            every { startTransformation(any()) } just Runs
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(dataDescriptors, any(), transformerId, nodeRefs, targetMediaType, parameters) } just Runs
        }

        ActiveMQAlfrescoPromenaService(completedTransformationManager, alfrescoDataDescriptorGetter, transformerSender)
                .transform(transformerId, nodeRefs, targetMediaType, parameters, duration) shouldBe resultNodeRefs
    }

    @Test
    fun `transform _ null parameters`() {
        val duration = Duration.ofSeconds(5)

        val completedTransformationManager = mockk<CompletedTransformationManager> {
            every { getTransformedNodeRefs(any(), duration) } returns resultNodeRefs

            every { startTransformation(any()) } just Runs
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(dataDescriptors, any(), transformerId, nodeRefs, targetMediaType, MapParameters.empty()) } just Runs
        }

        ActiveMQAlfrescoPromenaService(completedTransformationManager, alfrescoDataDescriptorGetter, transformerSender)
                .transform(transformerId, nodeRefs, targetMediaType, null, duration) shouldBe resultNodeRefs
    }

    @Test
    fun `transform _ throw TransformationSynchronizationException`() {
        val parameters = MapParameters.empty()

        val completedTransformationManager = mockk<CompletedTransformationManager> {
            every { getTransformedNodeRefs(any(), null) } throws
                    TransformationSynchronizationException(transformerId, nodeRefs, targetMediaType, parameters, null)

            every { startTransformation(any()) } just Runs
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(dataDescriptors, any(), transformerId, nodeRefs, targetMediaType, parameters) } just Runs
        }

        shouldThrow<TransformationSynchronizationException> {
            ActiveMQAlfrescoPromenaService(completedTransformationManager, alfrescoDataDescriptorGetter, transformerSender)
                    .transform(transformerId, nodeRefs, targetMediaType, parameters, null)
        }
    }

    @Test
    fun transformAsync() {
        val completedTransformationManager = mockk<CompletedTransformationManager>()

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(dataDescriptors, any(), transformerId, nodeRefs, targetMediaType, MapParameters.empty()) } just Runs
        }

        ActiveMQAlfrescoPromenaService(completedTransformationManager, alfrescoDataDescriptorGetter, transformerSender)
                .transformAsync(transformerId, nodeRefs, targetMediaType, null)
    }
}