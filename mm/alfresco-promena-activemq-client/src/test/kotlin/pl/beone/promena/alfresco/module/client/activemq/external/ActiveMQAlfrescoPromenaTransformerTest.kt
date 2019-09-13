package pl.beone.promena.alfresco.module.client.activemq.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import reactor.core.publisher.Mono
import java.time.Duration

class ActiveMQAlfrescoPromenaTransformerTest {

    companion object {
        private val nodeRefs = listOf(
            NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"),
            NodeRef("workspace://SpacesStore/a36d5c1a-e32c-478b-ad8b-14b2882115d1")
        )
        private const val nodesChecksum = "123456789"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val dataDescriptors = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
        private val communicationParameters = memoryCommunicationParameters()
        private val transformationDescriptor = transformationDescriptor(transformation, dataDescriptors, communicationParameters)
        private val retry = noRetry()
        private val resultNodeRefs = listOf(
            NodeRef("workspace://SpacesStore/c0b95525-26a6-4067-9756-6bec11c93c70"),
            NodeRef("workspace://SpacesStore/6967ef69-2768-411e-8ab4-4dc66001911b")
        )
    }

    @Test
    fun transform() {
        val renditionName = "doclib"

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns Mono.just(resultNodeRefs)
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(any(), transformationDescriptor, nodeRefs, renditionName, nodesChecksum, retry, 0) } just Runs
        }

        ActiveMQAlfrescoPromenaTransformer(
            communicationParameters,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        ).transform(transformation, nodeRefs, renditionName, Duration.ofSeconds(5)) shouldBe
                resultNodeRefs
    }

    @Test
    fun `transform _ throw TransformationSynchronizationException`() {
        val duration = Duration.ofMillis(1500)

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns
                    Mono.error(TransformationSynchronizationException(transformation, nodeRefs, duration))
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(any(), transformationDescriptor, nodeRefs, null, nodesChecksum, retry, 0) } just Runs
        }

        shouldThrow<TransformationSynchronizationException> {
            ActiveMQAlfrescoPromenaTransformer(
                communicationParameters,
                retry,
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                reactiveTransformationManager,
                transformerSender
            ).transform(transformation, nodeRefs, null, duration)
        }
    }

    @Test
    fun transformAsync() {
        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns Mono.just(resultNodeRefs)
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every { send(any(), transformationDescriptor, nodeRefs, null, nodesChecksum, retry, 0) } just Runs
        }

        ActiveMQAlfrescoPromenaTransformer(
            communicationParameters,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        ).transformAsync(transformation, nodeRefs)
    }
}