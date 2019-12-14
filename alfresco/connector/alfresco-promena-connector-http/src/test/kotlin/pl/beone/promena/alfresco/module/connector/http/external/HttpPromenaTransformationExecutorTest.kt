package pl.beone.promena.alfresco.module.connector.http.external

import com.github.kittinunf.fuel.core.Headers.Companion.CONTENT_TYPE
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.StringBody.exact
import pl.beone.promena.alfresco.module.connector.http.TestConstants.dataDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.externalCommunicationParameters
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodeDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodeRefs
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodesChecksum
import pl.beone.promena.alfresco.module.connector.http.TestConstants.performedTransformationDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.performedTransformationDescriptorBytes
import pl.beone.promena.alfresco.module.connector.http.TestConstants.postTransformationExecutor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.retry
import pl.beone.promena.alfresco.module.connector.http.TestConstants.threads
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationDescriptorBytes
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationExecutionResult
import pl.beone.promena.alfresco.module.connector.http.TestConstants.userName
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.alfresco.module.core.external.transformation.manager.MemoryWithAlfrescoPersistencePromenaMutableTransformationManager
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import java.net.HttpURLConnection.HTTP_OK
import java.time.Duration

class HttpPromenaTransformationExecutorTest {

    private val promenaMutableTransformationManager =
        MemoryWithAlfrescoPersistencePromenaMutableTransformationManager(false, 100, Duration.ofSeconds(10), mockk(), mockk())

    private lateinit var mockServer: ClientAndServer

    private lateinit var postTransformationExecutorValidator: PostTransformationExecutorValidator
    private lateinit var nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier
    private lateinit var nodesChecksumGenerator: NodesChecksumGenerator
    private lateinit var dataDescriptorGetter: DataDescriptorGetter
    private lateinit var authorizationService: AuthorizationService
    private lateinit var serializationService: SerializationService

    private lateinit var httpPromenaTransformationExecutor: HttpPromenaTransformationExecutor

    @Before
    fun setUp() {
        postTransformationExecutorValidator = mockk {
            every { validate(any()) } just Runs
        }
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } just Runs
            every { verify(nodeRefs[1]) } just Runs
        }
        nodesChecksumGenerator = mockk {
            every { generate(nodeRefs) } returns nodesChecksum
        }
        dataDescriptorGetter = mockk {
            every { get(nodeDescriptor) } returns dataDescriptor
        }
        authorizationService = mockk {
            every { getCurrentUser() } returns userName
        }
        serializationService = mockk {
            every { serialize(transformationDescriptor) } returns transformationDescriptorBytes
            every {
                deserialize(performedTransformationDescriptorBytes, PerformedTransformationDescriptor::class.java)
            } returns performedTransformationDescriptor
        }

        startServer()

        httpPromenaTransformationExecutor = HttpPromenaTransformationExecutor(
            threads,
            "localhost:${mockServer.port}",
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            retry,
            postTransformationExecutorValidator,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            mockk(),
            dataDescriptorGetter,
            mockk(),
            mockk(),
            mockk(),
            authorizationService,
            mockk(),
            serializationService
        )
    }

    private fun startServer() {
        mockServer = ClientAndServer.startClientAndServer(0)
        mockServer
            .`when`(
                request()
                    .withMethod("POST")
                    .withPath("/transform")
                    .withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.mimeType)
                    .withBody(exact(String(transformationDescriptorBytes)))
            )
            .respond(
                response()
                    .withStatusCode(HTTP_OK)
                    .withBody(String(performedTransformationDescriptorBytes))
            )
    }

    @After
    fun stopServer() {
        mockServer.stop()
    }

    @Test
    fun `should go correct path`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                transformationExecutionResult

        promenaMutableTransformationManager.getResult(
            httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor)
        ) shouldBe transformationExecutionResult
    }

    @Test
    fun `should detect difference between nodes checksums, throw NodesInconsistencyException and stop further processing`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                "not equal"  // nodesChecksumGenerator.generate(nodeRefs)

        shouldThrow<NodesInconsistencyException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor),
                Duration.ofSeconds(2)
            )
        }.message shouldBe "Nodes <$nodeRefs> have changed in the meantime (old checksum <$nodesChecksum>, current checksum <not equal>)"
    }

    @Test
    fun `should detect that one of nodes doesn't exist, throw InvalidNodeRefException and stop further processing`() {
        every { authorizationService.runAs<Any>(userName, any()) } throws
                InvalidNodeRefException("Node <${nodeRefs[0]}> doesn't exist", nodeRefs[0])

        shouldThrow<InvalidNodeRefException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor),
                Duration.ofSeconds(2)
            )
        }.message shouldBe "Node <${nodeRefs[0]}> doesn't exist"
    }

    @Test
    fun `should throw RuntimeException during processing result and stop further processing`() {
        val exception = RuntimeException("exception")
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThenThrows  // nodesChecksumGenerator.generate(nodeRefs)
                exception

        shouldThrow<RuntimeException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor),
                Duration.ofSeconds(2)
            )
        }.message shouldBe exception.message
    }
}