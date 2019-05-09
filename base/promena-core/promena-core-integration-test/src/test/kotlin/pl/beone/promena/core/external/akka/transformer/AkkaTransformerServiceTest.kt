package pl.beone.promena.core.external.akka.transformer

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

class AkkaTransformerServiceTest {

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
        val metadata = mock<Metadata>()

        val data = mock<Data> { on { getBytes() } doReturn "test".toByteArray() }
        val data2 = mock<Data> { on { getBytes() } doReturn "test".toByteArray() }

        val dataDescriptors = listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN),
                                     DataDescriptor(data2, MediaTypeConstants.TEXT_PLAIN))

        val transformedDataDescriptor = TransformedDataDescriptor(data, metadata)
        val transformedDataDescriptor2 = TransformedDataDescriptor(data2, metadata)

        val transformer = mock<Transformer> {
            on { transform(dataDescriptors, MediaTypeConstants.APPLICATION_PDF, MapParameters.empty()) } doReturn
                    listOf(TransformedDataDescriptor(data, metadata), TransformedDataDescriptor(data2, metadata))
            on { canTransform(any(), any(), any()) } doReturn true
        }

        val internalCommunicationConverter =
                mock<InternalCommunicationConverter> {
                    on { convert(eq(transformedDataDescriptor)) } doReturn transformedDataDescriptor
                    on { convert(eq(transformedDataDescriptor2)) } doReturn transformedDataDescriptor2
                }

        val transformerService = prepare(transformer, internalCommunicationConverter)

        val transformedDataDescriptors = transformerService.transform("mock",
                                                                      dataDescriptors,
                                                                      MediaTypeConstants.APPLICATION_PDF,
                                                                      MapParameters.empty())

        transformedDataDescriptors.zip(dataDescriptors).forEach { (transformedDataDescriptor, dataDescriptor) ->
            assertThat(transformedDataDescriptor.data).isEqualTo(dataDescriptor.data)
            assertThat(transformedDataDescriptor.metadata).isEqualTo(metadata)
        }
    }

    @Test
    fun `transform _ transformers with not suitable parameters _ should throw TransformerNotFoundException`() {
        val transformerService = prepare(mock {
            on { canTransform(any(), eq(MediaTypeConstants.APPLICATION_PDF), any()) } doReturn true
            on { canTransform(any(), any(), any()) } doReturn false
        }, mock())

        assertThatThrownBy {
            transformerService.transform("mock",
                                         listOf(DataDescriptor(mock { on { getBytes() } doReturn "test".toByteArray() },
                                                               MediaTypeConstants.TEXT_PLAIN)),
                                         MediaTypeConstants.APPLICATION_PDF,
                                         mock {
                                             on { getTimeout() } doThrow NoSuchElementException("")
                                             on { toString() } doReturn "MapParameters(parameters={})"
                                         })
        }
                .isExactlyInstanceOf(TransformerNotFoundException::class.java)
                .hasMessage("Couldn't transform because there is no suitable transformer | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <1 source(s)>: [<MediaType(mimeType=text/plain, charset=UTF-8)>]")
                .hasStackTraceContaining("There is no transformer that can process it. There following <1> transformers are available:")
                .hasStackTraceContaining("pl.beone.promena.transformer.contract.Transformer")

        assertThatThrownBy {
            transformerService.transform("mock",
                                         emptyList(),
                                         MediaTypeConstants.TEXT_PLAIN,
                                         mock {
                                             on { getTimeout() } doThrow NoSuchElementException("")
                                             on { toString() } doReturn "MapParameters(parameters={})"
                                         })
        }
                .isExactlyInstanceOf(TransformerNotFoundException::class.java)
                .hasMessage("Couldn't transform because there is no suitable transformer | <mock> <MediaType(mimeType=text/plain, charset=UTF-8), {}> <0 source(s)>: []")
                .hasStackTraceContaining("There is no transformer that can process it. There following <1> transformers are available:")
                .hasStackTraceContaining("pl.beone.promena.transformer.contract.Transformer")
    }

    @Test
    fun `transform _ error during transformation _ should throw TransformerException`() {
        val akkaTransformerService = prepare(mock {
            on { transform(any(), any(), any()) } doThrow RuntimeException("Mock extension")
            on { canTransform(any(), any(), any()) } doReturn true
        }, mock())

        assertThatThrownBy {
            akkaTransformerService.transform("mock",
                                             emptyList(),
                                             MediaTypeConstants.APPLICATION_PDF,
                                             mock { on { getTimeout() } doThrow NoSuchElementException("") })
        }
                .isExactlyInstanceOf(TransformerException::class.java)
                .hasMessage("Couldn't transform because an error occurred | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []")
                .hasStackTraceContaining("RuntimeException")
                .hasStackTraceContaining("Mock extension")
    }

    @Test
    fun `transform _ timeout during transformation _ should throw TransformerTimeoutException`() {
        val akkaTransformerService = prepare(mock {
            on { transform(any(), any(), any()) } doThrow TransformerTimeoutException("Time expired")
            on { canTransform(any(), any(), any()) } doReturn true
        }, mock())

        assertThatThrownBy {
            akkaTransformerService.transform("mock",
                                             emptyList(),
                                             MediaTypeConstants.APPLICATION_PDF,
                                             mock { on { getTimeout() } doThrow NoSuchElementException("") })
        }
                .isExactlyInstanceOf(TransformerTimeoutException::class.java)
                .hasMessage("Couldn't transform because transformation time <21474835000> has expired | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []")
                .hasStackTraceContaining("Time expired")
    }

    @Test
    fun `transform _ timeout on Akka level _ should throw TransformerTimeoutException`() {
        val akkaTransformerService = prepare(mock {
            on { transform(any(), any(), any()) } doAnswer {
                Thread.sleep(2000)
                emptyList()
            }
            on { canTransform(any(), any(), any()) } doReturn true
        }, mock())

        assertThatThrownBy {
            akkaTransformerService.transform("mock",
                                             emptyList(),
                                             MediaTypeConstants.APPLICATION_PDF,
                                             mock { on { getTimeout() } doReturn 100 })
        }
                .isExactlyInstanceOf(TransformerTimeoutException::class.java)
                .hasMessage("Couldn't transform because transformation time <100> has expired | <mock> <MediaType(mimeType=application/pdf, charset=UTF-8), {}> <0 source(s)>: []")
    }

    private fun prepare(transformer: Transformer, internalCommunicationConverter: InternalCommunicationConverter): AkkaTransformerService {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val mirrorTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java, listOf(transformer), internalCommunicationConverter), "mock"
        )
        val emptyTransformerActorRef = actorSystem.actorOf(
                Props.create(TransformerActor::class.java, emptyList<Transformer>(), internalCommunicationConverter), "empty"
        )

        val actorService = mock<ActorService> {
            on { getTransformationActor("mock") } doReturn mirrorTransformerActorRef
            on { getTransformationActor("empty") } doReturn emptyTransformerActorRef
        }

        return AkkaTransformerService(actorMaterializer, actorService)
    }
}