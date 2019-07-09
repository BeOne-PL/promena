package pl.beone.promena.alfresco.module.client.http.external

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Before
import org.junit.Test
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.Memory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.DisposableServer
import reactor.netty.http.client.HttpClient
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerRequest
import reactor.test.StepVerifier
import reactor.test.expectError
import java.io.File
import java.net.URI
import java.time.Duration

private data class Mocks(val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                         val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                         val serializationService: SerializationService)

class HttpClientAlfrescoPromenaServiceTest {

    companion object {
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"))
        private val targetMediaType = TEXT_PLAIN
        private val parameters = MapParameters(mapOf("key" to "value"))
        private val dataDescriptors = listOf(DataDescriptor(MemoryData("test".toByteArray()), TEXT_PLAIN, MapMetadata(mapOf("key" to "value"))))
        private val transformationDescriptor = TransformationDescriptor(dataDescriptors, targetMediaType, parameters)
        private val transformedDataDescriptors = listOf(TransformedDataDescriptor(MemoryData("test".toByteArray()), MapMetadata.empty()))
        private val serverException = RuntimeException("Exception")
        private val transformedNodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        private lateinit var httpServer: DisposableServer

        private val successBytes = "/success.bin".readBytes()
        private val exceptionBytes = "/exception.bin".readBytes()
    }

    @Before
    fun server() {
        httpServer = startServer()
    }

    @Test
    fun transform() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()
        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                         false,
                                         0,
                                         Duration.ofMillis(0),
                                         alfrescoNodesChecksumGenerator,
                                         alfrescoDataDescriptorGetter,
                                         alfrescoTransformedDataDescriptorSaver,
                                         serializationService,
                                         httpServer.createHttpClient())
                .transform("success", nodeRefs, targetMediaType, parameters) shouldBe transformedNodeRefs
    }

    @Test
    fun `transform _ with communication location`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()
        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        val tmpFile = createTempFile()

        HttpClientAlfrescoPromenaService(ExternalCommunication(File, tmpFile.toURI()),
                                         false,
                                         0,
                                         Duration.ofMillis(0),
                                         alfrescoNodesChecksumGenerator,
                                         alfrescoDataDescriptorGetter,
                                         alfrescoTransformedDataDescriptorSaver,
                                         serializationService,
                                         httpServer.createHttpClient())
                .transform("success", nodeRefs, targetMediaType, parameters) shouldBe transformedNodeRefs

        // test server should remove file from "location" if URI contains "location" param
        tmpFile.exists() shouldBe false
    }

    @Test
    fun `transform _ timeout expires before the end of transformation _ should throw TransformationSynchronizationException and finish transformation after it`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()
        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        shouldThrow<TransformationSynchronizationException> {
            HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                             false,
                                             0,
                                             Duration.ofMillis(0),
                                             alfrescoNodesChecksumGenerator,
                                             alfrescoDataDescriptorGetter,
                                             alfrescoTransformedDataDescriptorSaver,
                                             serializationService,
                                             httpServer.createHttpClient())
                    .transform("success", nodeRefs, targetMediaType, parameters, Duration.ofMillis(0))
        }

        Thread.sleep(500)

        verify { alfrescoTransformedDataDescriptorSaver.save("success", nodeRefs, targetMediaType, transformedDataDescriptors) }
    }

    @Test
    fun transformAsync() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()
        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        StepVerifier.create(HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                                             false,
                                                             0,
                                                             Duration.ofMillis(0),
                                                             alfrescoNodesChecksumGenerator,
                                                             alfrescoDataDescriptorGetter,
                                                             alfrescoTransformedDataDescriptorSaver,
                                                             serializationService,
                                                             httpServer.createHttpClient())
                                    .transformAsync("success", nodeRefs, targetMediaType, parameters))
                .expectNext(transformedNodeRefs)
                .expectComplete()
                .verify()
    }

    @Test
    fun `transformAsync _ exception on server side _ should throw RuntimeException`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        StepVerifier.create(HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                                             false,
                                                             0,
                                                             Duration.ofMillis(0),
                                                             alfrescoNodesChecksumGenerator,
                                                             alfrescoDataDescriptorGetter,
                                                             alfrescoTransformedDataDescriptorSaver,
                                                             serializationService,
                                                             httpServer.createHttpClient())
                                    .transformAsync("exception", nodeRefs, targetMediaType, parameters))
                .expectErrorSatisfies {
                    it shouldBeSameInstanceAs serverException
                    it.message shouldBe serverException.message
                }
                .verify()
    }

    @Test
    fun `transformAsync _ nodes checksum were changed in the meantime _ should throw AnotherTransformationIsInProgressException`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returnsMany listOf("123456789", "987654321")
        }

        StepVerifier.create(HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                                             false,
                                                             0,
                                                             Duration.ofMillis(0),
                                                             alfrescoNodesChecksumGenerator,
                                                             alfrescoDataDescriptorGetter,
                                                             alfrescoTransformedDataDescriptorSaver,
                                                             serializationService,
                                                             httpServer.createHttpClient())
                                    .transformAsync("success", nodeRefs, targetMediaType, parameters))
                .expectError(AnotherTransformationIsInProgressException::class)
                .verify()
    }

    @Test
    fun `transformAsync _ error on server side but nodes checksum were changed in the meantime _ should throw AnotherTransformationIsInProgressException`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returnsMany listOf("123456789", "987654321")
        }

        StepVerifier.create(HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                                             false,
                                                             0,
                                                             Duration.ofMillis(0),
                                                             alfrescoNodesChecksumGenerator,
                                                             alfrescoDataDescriptorGetter,
                                                             alfrescoTransformedDataDescriptorSaver,
                                                             serializationService,
                                                             httpServer.createHttpClient())
                                    .transformAsync("exception", nodeRefs, targetMediaType, parameters))
                .expectError(AnotherTransformationIsInProgressException::class)
                .verify()
    }

    @Test
    fun `transformAsync _ exception on server side with retry policy _ should throw RetryExhaustedException with RuntimeException on stack`() {
        val (alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService) = mock()

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }

        StepVerifier.create(HttpClientAlfrescoPromenaService(ExternalCommunication(Memory),
                                                             true,
                                                             3,
                                                             Duration.ofMillis(300),
                                                             alfrescoNodesChecksumGenerator,
                                                             alfrescoDataDescriptorGetter,
                                                             alfrescoTransformedDataDescriptorSaver,
                                                             serializationService,
                                                             httpServer.createHttpClient())
                                    .transformAsync("exception", nodeRefs, targetMediaType, parameters))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(600))
                .expectErrorSatisfies {
                    it shouldBeSameInstanceAs serverException
                    it.message shouldBe serverException.message
                }
                .verify()
    }

    private fun startServer(): DisposableServer =
            HttpServer.create()
                    .port(0)
                    .wiretap(true)
                    .route {
                        it.post("/transform/success") { request, response ->
                            deleteFileFromLocationPath(request)

                            response.send(ByteBufFlux.fromInbound(Mono.just(successBytes)))
                        }
                        it.post("/transform/exception") { _, response ->
                            response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                                    .header("serialization-class", "java.lang.RuntimeException")
                                    .send(ByteBufFlux.fromInbound(Mono.just(exceptionBytes)))
                        }
                    }.bindNow()

    // a bit stupid. If URI contain "location" - remove that file. Only for test purposes
    private fun deleteFileFromLocationPath(request: HttpServerRequest) {
        val location = QueryStringDecoder(request.uri()).parameters()["location"]
        if (location != null) {
            File(URI(location.first())).delete()
        }
    }

    private fun DisposableServer.createHttpClient(): HttpClient =
            HttpClient.create()
                    .port(address().port)

    private fun mock(): Mocks {
        val alfrescoDataDescriptorGetter = mockk<AlfrescoDataDescriptorGetter> {
            every { get(nodeRefs) } returns dataDescriptors
        }

        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns successBytes
            every { deserialize(successBytes, getClazz<List<TransformedDataDescriptor>>()) } returns transformedDataDescriptors

            every { deserialize(exceptionBytes, getClazz<RuntimeException>()) } returns serverException
        }

        val alfrescoTransformedDataDescriptorSaver = mockk<AlfrescoTransformedDataDescriptorSaver> {
            every { save("success", nodeRefs, targetMediaType, transformedDataDescriptors) } returns transformedNodeRefs
        }

        return Mocks(alfrescoDataDescriptorGetter, alfrescoTransformedDataDescriptorSaver, serializationService)
    }
}


private fun String.readBytes(): ByteArray =
        object {}::class.java.getResourceAsStream(this).readAllBytes()