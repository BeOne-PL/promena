package pl.beone.promena.alfresco.module.connector.http.external

import com.github.kittinunf.fuel.core.Headers.Companion.CONTENT_TYPE
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.shouldThrowExactly
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.StringBody.exact
import pl.beone.promena.alfresco.module.connector.http.TestConstants
import pl.beone.promena.alfresco.module.connector.http.TestConstants.dataDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.externalCommunicationParameters
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodeDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodeRefs
import pl.beone.promena.alfresco.module.connector.http.TestConstants.nodesChecksum
import pl.beone.promena.alfresco.module.connector.http.TestConstants.postTransformationExecutor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.retry
import pl.beone.promena.alfresco.module.connector.http.TestConstants.threads
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationDescriptor
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationDescriptorBytes
import pl.beone.promena.alfresco.module.connector.http.TestConstants.transformationExecutionResult
import pl.beone.promena.alfresco.module.connector.http.TestConstants.userName
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.alfresco.module.core.internal.transformation.ConcurrentPromenaMutableTransformationManager
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.lib.connector.http.applicationmodel.exception.HttpException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.concurrent.TimeoutException

class HttpPromenaTransformationExecutorRetryTest {

    companion object {
        private val exception = TransformationException("exception", RuntimeException::class.java)
        private val exceptionBytes = "exception to deserialize".toByteArray()
    }

    private val promenaMutableTransformationManager = ConcurrentPromenaMutableTransformationManager(100, Duration.ofMinutes(1))

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
            every { deserialize(exceptionBytes, exception::class.java) } returns exception
            every {
                deserialize(TestConstants.performedTransformationDescriptorBytes, PerformedTransformationDescriptor::class.java)
            } returns TestConstants.performedTransformationDescriptor
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
            authorizationService,
            mockk(),
            serializationService
        )
    }

    private fun startServer() {
        mockServer = ClientAndServer.startClientAndServer(0)
        mockServer.`when`(
            request()
                .withMethod("POST")
                .withPath("/transform")
                .withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.mimeType)
                .withBody(exact(String(transformationDescriptorBytes))),
            Times.exactly(4)
        )
            .respond(
                response()
                    .withStatusCode(HTTP_INTERNAL_ERROR)
                    .withHeader(SERIALIZATION_CLASS, exception::class.java.canonicalName)
                    .withBody(String(exceptionBytes))
            )
        mockServer.`when`(
            request()
                .withMethod("POST")
                .withPath("/transform")
                .withHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM.mimeType)
                .withBody(exact(String(transformationDescriptorBytes)))
        )
            .respond(
                response()
                    .withStatusCode(HttpURLConnection.HTTP_OK)
                    .withBody(String(TestConstants.performedTransformationDescriptorBytes))
            )
    }

    @After
    fun stopServer() {
        mockServer.stop()
    }

    @Test
    fun `should throw exception immediately after error because of no retry policy`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum // nodesChecksumGenerator.generate(nodeRefs)

        shouldThrowExactly<TransformationException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor, noRetry()),
                ofSeconds(3)
            )
        }.message shouldBe exception.message
    }

    @Test
    fun `should use default retry policy and throw exception after a few attempts`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum // nodesChecksumGenerator.generate(nodeRefs)

        val transformationExecution = httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor)

        shouldThrowExactly<TimeoutException> {
            promenaMutableTransformationManager.getResult(
                transformationExecution,
                ofSeconds(5)
            )
        }

        shouldThrowExactly<TransformationException> {
            promenaMutableTransformationManager.getResult(
                transformationExecution,
                ofSeconds(2)
            )
        }.message shouldBe exception.message
    }

    @Test
    fun `should complete transformation after a few attempts`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                transformationExecutionResult

        val transformationExecution =
            httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor, customRetry(4, ofSeconds(1)))

        shouldThrowExactly<TimeoutException> {
            promenaMutableTransformationManager.getResult(
                transformationExecution,
                ofSeconds(3)
            )
        }

        promenaMutableTransformationManager.getResult(
            transformationExecution,
            ofSeconds(2)
        ) shouldBe transformationExecutionResult
    }

    @Test
    fun `should detect difference between nodes checksums, throw NodesInconsistencyException and stop further retrying`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                "not equal"  // nodesChecksumGenerator.generate(nodeRefs)

        shouldThrow<NodesInconsistencyException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor, customRetry(5, ofSeconds(1))),
                ofSeconds(2)
            )
        }.message shouldBe "Nodes <$nodeRefs> have changed in the meantime (old checksum <$nodesChecksum>, current checksum <not equal>)"
    }

    @Test
    fun `should throw RuntimeException during processing exception and stop further retrying`() {
        val runtimeException = RuntimeException("exception")

        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThenThrows  // nodesExistenceVerifier.verify(nodeRefs)
                runtimeException // nodesChecksumGenerator.generate(nodeRefs)

        shouldThrow<RuntimeException> {
            promenaMutableTransformationManager.getResult(
                httpPromenaTransformationExecutor.execute(transformation, nodeDescriptor, postTransformationExecutor, customRetry(5, ofSeconds(1))),
                ofSeconds(3)
            )
        }.message shouldBe runtimeException.message
    }

    @Test
    fun `should throw HttpException during making request and should complete transformation after a few attempts`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum // nodesChecksumGenerator.generate(nodeRefs)

        val transformationExecution = HttpPromenaTransformationExecutor(
            threads,
            "localhost:1",
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
            authorizationService,
            mockk(),
            serializationService
        ).execute(transformation, nodeDescriptor, postTransformationExecutor, customRetry(4, ofSeconds(1)))

        shouldThrowExactly<TimeoutException> {
            promenaMutableTransformationManager.getResult(
                transformationExecution,
                ofSeconds(3)
            )
        }

        shouldThrowExactly<HttpException> {
            promenaMutableTransformationManager.getResult(
                transformationExecution,
                ofSeconds(2)
            )
        }
    }
}