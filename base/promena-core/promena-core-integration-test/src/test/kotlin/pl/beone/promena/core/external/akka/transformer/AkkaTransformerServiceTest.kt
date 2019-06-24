package pl.beone.promena.core.external.akka.transformer

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.communication.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.TransformerActor
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.io.PrintWriter
import java.io.StringWriter


class AkkaTransformerServiceTest {

    companion object {
        private const val transformerId = "mock"
        private const val emptyTransformerId = "empty"
        private val bytes = "test".toByteArray()
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
    }

    private lateinit var actorSystem: ActorSystem

    @Before
    fun setUp() {
        actorSystem = ActorSystem.create()
    }

    @After
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun transform() {
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val metadata = mockk<Metadata>()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
        }
        val data2 = mockk<Data> {
            every { getBytes() } returns bytes
        }

        val dataDescriptors = listOf(DataDescriptor(data, mediaType),
                                     DataDescriptor(data2, mediaType))

        val transformedDataDescriptor = TransformedDataDescriptor(data, metadata)
        val transformedDataDescriptor2 = TransformedDataDescriptor(data2, metadata)

        val transformer = mockk<Transformer> {
            every { transform(dataDescriptors, targetMediaType, MapParameters.empty()) } returns listOf(transformedDataDescriptor,
                                                                                                        transformedDataDescriptor2)
            every { canTransform(any(), any(), any()) } returns true
        }

        val internalCommunicationConverter = mockk<InternalCommunicationConverter> {
            every { convert(eq(transformedDataDescriptor)) } returns transformedDataDescriptor
            every { convert(eq(transformedDataDescriptor2)) } returns transformedDataDescriptor2
        }

        val transformerService = prepare(transformer, internalCommunicationConverter)

        val transformedDataDescriptors = transformerService.transform(transformerId,
                                                                      dataDescriptors,
                                                                      targetMediaType,
                                                                      MapParameters.empty())

        transformedDataDescriptors.zip(dataDescriptors).forEach { (transformedDataDescriptor, dataDescriptor) ->
            transformedDataDescriptor.data shouldBe dataDescriptor.data
            transformedDataDescriptor.metadata shouldBe metadata
        }
    }

    @Test
    fun `transform _ transformers with not suitable parameters _ should throw TransformerNotFoundException`() {
        val transformerService = prepare(mockk {
            every { canTransform(any(), targetMediaType, any()) } returns true
            every { canTransform(any(), any(), any()) } returns false
        }, mockk())

        val dataDescriptors = listOf(DataDescriptor(mockk { every { getBytes() } returns bytes },
                                                    MediaTypeConstants.TEXT_PLAIN))

        val parameters = mockk<Parameters> {
            every { getTimeout() } throws NoSuchElementException("")
            every { toString() } returns "MapParameters(parameters={})"
        }

        shouldThrow<TransformerNotFoundException> {
            transformerService.transform(transformerId, dataDescriptors, targetMediaType, parameters)
        }.apply {
            this.message shouldBe "Couldn't transform because there is no suitable transformer | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <1 source(s)>: [<MediaType(mimeType=text/plain, charset=UTF-8)>]"
            this.getStringStackTrace() shouldContain "There is no transformer that can process it. There following <1> transformers are available:"
            this.getStringStackTrace() shouldContain "pl.beone.promena.transformer.contract.Transformer"
        }

        shouldThrow<TransformerNotFoundException> {
            transformerService.transform(transformerId, emptyList(), MediaTypeConstants.TEXT_PLAIN, parameters)
        }.apply {
            this.message shouldBe "Couldn't transform because there is no suitable transformer | <mock> <MediaType(mimeType=text/plain, charset=UTF-8), {}> <0 source(s)>: []"
            this.getStringStackTrace() shouldContain "There is no transformer that can process it. There following <1> transformers are available:"
            this.getStringStackTrace() shouldContain "pl.beone.promena.transformer.contract.Transformer"
        }
    }

    @Test
    fun `transform _ error during transformation _ should throw TransformerException`() {
        val akkaTransformerService = prepare(mockk {
            every { transform(any(), any(), any()) } throws RuntimeException("Mock extension")
            every { canTransform(any(), any(), any()) } returns true
        }, mockk())

        val parameters = mockk<Parameters> {
            every { getTimeout() } throws NoSuchElementException("")
        }

        shouldThrow<TransformerException> {
            akkaTransformerService.transform(transformerId, emptyList(), targetMediaType, parameters)
        }.apply {
            this.message shouldBe "Couldn't transform because an error occurred | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []"
            this.getStringStackTrace() shouldContain "RuntimeException"
            this.getStringStackTrace() shouldContain "Mock extension"
        }
    }

    @Test
    fun `transform _ timeout during transformation _ should throw TransformerTimeoutException`() {
        val akkaTransformerService = prepare(mockk {
            every { transform(any(), any(), any()) } throws TransformerTimeoutException("Time expired")
            every { canTransform(any(), any(), any()) } returns true
        }, mockk())

        val parameters = mockk<Parameters> {
            every { getTimeout() } throws NoSuchElementException("")
        }

        shouldThrow<TransformerTimeoutException> {
            akkaTransformerService.transform(transformerId, emptyList(), targetMediaType, parameters)
        }.apply {
            this.message shouldBe "Couldn't transform because transformation time <21474835000> has expired | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []"
            this.getStringStackTrace() shouldContain "Time expired"
        }
    }

    @Test
    fun `transform _ timeout on Akka level _ should throw TransformerTimeoutException`() {
        val akkaTransformerService = prepare(mockk {
            every { transform(any(), any(), any()) } answers {
                Thread.sleep(2000)
                emptyList()
            }
            every { canTransform(any(), any(), any()) } returns true
        }, mockk())

        val parameters = mockk<Parameters> {
            every { getTimeout() } returns 100
        }

        shouldThrow<TransformerTimeoutException> {
            akkaTransformerService.transform(transformerId, emptyList(), targetMediaType, parameters)
        }.apply {
            this.message shouldBe "Couldn't transform because transformation time <100> has expired | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []"
        }
    }

    private fun prepare(transformer: Transformer, internalCommunicationConverter: InternalCommunicationConverter): AkkaTransformerService {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val mirrorTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) { TransformerActor(listOf(transformer), internalCommunicationConverter) }, transformerId
        )
        val emptyTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java) { TransformerActor(emptyList(), internalCommunicationConverter) }, emptyTransformerId
        )

        val actorService = mockk<ActorService> {
            every { getTransformationActor(transformerId) } returns mirrorTransformerActorRef
            every { getTransformationActor(emptyTransformerId) } returns emptyTransformerActorRef
        }

        return AkkaTransformerService(actorMaterializer, actorService)
    }
}

private fun Exception.getStringStackTrace(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    this.printStackTrace(pw)
    return sw.toString()
}
