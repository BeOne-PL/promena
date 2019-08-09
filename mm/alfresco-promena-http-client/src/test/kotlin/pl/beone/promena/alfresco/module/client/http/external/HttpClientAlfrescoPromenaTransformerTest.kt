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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.reactivestreams.Publisher
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import reactor.core.publisher.Mono
import reactor.netty.ByteBufFlux
import reactor.netty.DisposableServer
import reactor.netty.NettyOutbound
import reactor.netty.http.client.HttpClient
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerRequest
import reactor.netty.http.server.HttpServerResponse
import reactor.test.StepVerifier
import reactor.test.expectError
import java.time.Duration

class HttpClientAlfrescoPromenaTransformerTest {

    companion object {
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"))
        private val transformation = singleTransformation("transformer", TEXT_PLAIN, emptyParameters() + ("key" to "value"))
        private val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
        private val transformationDescriptor = transformationDescriptor(transformation, dataDescriptor)
        private val transformedDataDescriptor = singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata())
        private val performedTransformationDescriptor = performedTransformationDescriptor(transformation, transformedDataDescriptor)
        private val transformedNodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))
        private const val userName = "admin"
        private lateinit var alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter
        private lateinit var alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver
        private lateinit var alfrescoAuthenticationService: AlfrescoAuthenticationService

        private lateinit var httpServer: DisposableServer
    }

    @Before
    fun setUp() {
        alfrescoDataDescriptorGetter = mockk {
            every { get(nodeRefs) } returns dataDescriptor
        }
        alfrescoTransformedDataDescriptorSaver = mockk {
            every { save(transformation, nodeRefs, transformedDataDescriptor) } returns transformedNodeRefs
        }
        alfrescoAuthenticationService = mockk {
            every { getCurrentUser() } returns userName
            every { runAs<List<NodeRef>>(userName, any()) } returns transformedNodeRefs
        }
    }

    @After
    fun stopServer() {
        httpServer.disposeNow()
    }

    @Test
    fun transform() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .then(Mono.just(serializedPerformedTransformationDescriptor)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        HttpClientAlfrescoPromenaTransformer(
            ExternalCommunication(ExternalCommunicationConstants.Memory),
            noRetry(),
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            alfrescoTransformedDataDescriptorSaver,
            serializationService,
            alfrescoAuthenticationService,
            httpServer.createHttpClient()
        )
            .transform(transformation, nodeRefs) shouldBe transformedNodeRefs
    }

    @Test
    fun `transform _ with communication location`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        val tmpDirUri = createTempDir().toURI()

        httpServer = startServer { request, response ->
            val location = QueryStringDecoder(request.uri()).parameters()["location"]

            val serialized = if (location == null || location.first() != tmpDirUri.toString()) {
                "not expected".toByteArray()
            } else {
                serializedPerformedTransformationDescriptor
            }
            response.send(serialized)
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        HttpClientAlfrescoPromenaTransformer(
            ExternalCommunication(ExternalCommunicationConstants.File, tmpDirUri),
            noRetry(),
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            alfrescoTransformedDataDescriptorSaver,
            serializationService,
            alfrescoAuthenticationService,
            httpServer.createHttpClient()
        )
            .transform(transformation, nodeRefs) shouldBe transformedNodeRefs
    }

    @Test
    fun `transform _ timeout expires before the end of transformation _ should throw TransformationSynchronizationException and finish transformation after it`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .then(Mono.just(serializedPerformedTransformationDescriptor)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        shouldThrow<TransformationSynchronizationException> {
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transform(transformation, nodeRefs, Duration.ofMillis(0))
        }

        Thread.sleep(500)

        verify(exactly = 1) { alfrescoAuthenticationService.runAs(userName, any()) }
    }

    @Test
    fun transformAsync() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .then(Mono.just(serializedPerformedTransformationDescriptor)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs)
        )
            .expectNext(transformedNodeRefs)
            .expectComplete()
            .verify()
    }

    @Test
    fun `transformAsync _ exception on server side _ should throw RuntimeException`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serverException = RuntimeException("Exception")
        val serializedServerException = "serverException".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .doOnNext {
                        response.header(PromenaHttpHeaders.SERIALIZATION_CLASS, "java.lang.RuntimeException")
                            .status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    }
                    .then(Mono.just(serializedServerException)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every { deserialize(serializedServerException, getClazz<RuntimeException>()) } returns serverException
        }

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs)
        )
            .expectErrorSatisfies {
                it shouldBeSameInstanceAs serverException
                it.message shouldBe serverException.message
            }
            .verify()
    }

    @Test
    fun `transformAsync _ nodes checksum were changed in the meantime _ should throw AnotherTransformationIsInProgressException`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .then(Mono.just(serializedPerformedTransformationDescriptor)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returnsMany listOf("123456789", "987654321")
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs)
        )
            .expectError(AnotherTransformationIsInProgressException::class)
            .verify()
    }

    @Test
    fun `transformAsync _ error on server side but nodes checksum were changed in the meantime _ should throw AnotherTransformationIsInProgressException`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serializedPerformedTransformationDescriptor = "performedTransformationDescriptor".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .then(Mono.just(serializedPerformedTransformationDescriptor)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returnsMany listOf("123456789", "987654321")
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every {
                deserialize(serializedPerformedTransformationDescriptor, getClazz<PerformedTransformationDescriptor>())
            } returns performedTransformationDescriptor
        }

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs)
        )
            .expectError(AnotherTransformationIsInProgressException::class)
            .verify()
    }

    @Test
    fun `transformAsync _ exception on server side with retry policy _ should throw RetryExhaustedException with RuntimeException on stack`() {
        val serializedTransformationDescriptor = "transformationDescriptor".toByteArray()
        val serverException = RuntimeException("Exception")
        val serializedServerException = "serverException".toByteArray()

        httpServer = startServer { request, response ->
            response.sendByteArray(
                request.receive()
                    .asByteArray()
                    .map { it shouldBe serializedTransformationDescriptor }
                    .doOnNext {
                        response.header(PromenaHttpHeaders.SERIALIZATION_CLASS, "java.lang.RuntimeException")
                            .status(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    }
                    .then(Mono.just(serializedServerException)))
        }

        val alfrescoNodesChecksumGenerator = mockk<AlfrescoNodesChecksumGenerator> {
            every { generateChecksum(nodeRefs) } returns "123456789"
        }
        val serializationService = mockk<SerializationService> {
            every { serialize(transformationDescriptor) } returns serializedTransformationDescriptor
            every { deserialize(serializedServerException, getClazz<RuntimeException>()) } returns serverException
        }

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                customRetry(3, Duration.ofMillis(300)),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs)
        )
            .expectSubscription()
            .expectNoEvent(Duration.ofMillis(600))
            .expectErrorSatisfies {
                it shouldBeSameInstanceAs serverException
                it.message shouldBe serverException.message
            }
            .verify()

        StepVerifier.create(
            HttpClientAlfrescoPromenaTransformer(
                ExternalCommunication(ExternalCommunicationConstants.Memory),
                noRetry(),
                alfrescoNodesChecksumGenerator,
                alfrescoDataDescriptorGetter,
                alfrescoTransformedDataDescriptorSaver,
                serializationService,
                alfrescoAuthenticationService,
                httpServer.createHttpClient()
            )
                .transformAsync(transformation, nodeRefs, customRetry(3, Duration.ofMillis(300)))
        )
            .expectSubscription()
            .expectNoEvent(Duration.ofMillis(600))
            .expectErrorSatisfies {
                it shouldBeSameInstanceAs serverException
                it.message shouldBe serverException.message
            }
            .verify()
    }

    private fun HttpServerResponse.send(data: ByteArray): NettyOutbound =
        send(ByteBufFlux.fromInbound(Mono.just(data)))

    private fun startServer(handler: (request: HttpServerRequest, response: HttpServerResponse) -> Publisher<Void>): DisposableServer =
        HttpServer.create()
            .port(0)
            .route {
                it.post("/transform") { request, response -> handler(request, response) }
            }.bindNow()


    private fun DisposableServer.createHttpClient(): HttpClient =
        HttpClient.create()
            .port(address().port)
}