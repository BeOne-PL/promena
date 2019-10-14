package pl.beone.promena.alfresco.module.connector.activemq.external

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.contract.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
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

class ActiveMQPromenaTransformerTest {

    companion object {
        private val nodeDescriptors = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
        )
        private val nodeRefs = nodeDescriptors.toNodeRefs()
        private const val nodesChecksum = "123456789"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val dataDescriptors = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
        private val communicationParameters = memoryCommunicationParameters()
        private val transformationDescriptor = transformationDescriptor(
            transformation,
            dataDescriptors,
            communicationParameters
        )
        private val retry = noRetry()
        private val resultNodeRefs = listOf(
            NodeRef("workspace://SpacesStore/c0b95525-26a6-4067-9756-6bec11c93c70"),
            NodeRef("workspace://SpacesStore/6967ef69-2768-411e-8ab4-4dc66001911b")
        )
    }

    @Test
    fun transform() {
        val nodesChecksumGenerator = mockk<NodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns Mono.just(resultNodeRefs)
        }

        val dataDescriptorGetter = mockk<DataDescriptorGetter> {
            every { get(nodeDescriptors) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every {
                send(
                    any(),
                    transformationDescriptor,
                    nodeDescriptors,
                    nodesChecksum,
                    retry, 0
                )
            } just Runs
        }

        ActiveMQPromenaTransformer(
            communicationParameters,
            retry,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        ).transform(
            transformation,
            nodeDescriptors, Duration.ofSeconds(5)
        ) shouldBe
                resultNodeRefs
    }

    @Test
    fun `transform _ throw TransformationSynchronizationException`() {
        val duration = Duration.ofMillis(1500)

        val nodesChecksumGenerator = mockk<NodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns
                    Mono.error(
                        TransformationSynchronizationException(
                            transformation,
                            nodeDescriptors,
                            duration
                        )
                    )
        }

        val dataDescriptorGetter = mockk<DataDescriptorGetter> {
            every { get(nodeDescriptors) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every {
                send(
                    any(),
                    transformationDescriptor,
                    nodeDescriptors,
                    nodesChecksum,
                    retry, 0
                )
            } just Runs
        }

        shouldThrow<TransformationSynchronizationException> {
            ActiveMQPromenaTransformer(
                communicationParameters,
                retry,
                nodesChecksumGenerator,
                dataDescriptorGetter,
                reactiveTransformationManager,
                transformerSender
            ).transform(
                transformation,
                nodeDescriptors, duration
            )
        }
    }

    @Test
    fun transformAsync() {
        val reactiveTransformationManager = mockk<ReactiveTransformationManager> {
            every { startTransformation(any()) } returns Mono.just(resultNodeRefs)
        }

        val nodesChecksumGenerator = mockk<NodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns nodesChecksum
        }

        val dataDescriptorGetter = mockk<DataDescriptorGetter> {
            every { get(nodeDescriptors) } returns dataDescriptors
        }

        val transformerSender = mockk<TransformerSender> {
            every {
                send(
                    any(),
                    transformationDescriptor,
                    nodeDescriptors,
                    nodesChecksum,
                    retry, 0
                )
            } just Runs
        }

        ActiveMQPromenaTransformer(
            communicationParameters,
            retry,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        ).transformAsync(
            transformation,
            nodeDescriptors
        )
    }
}