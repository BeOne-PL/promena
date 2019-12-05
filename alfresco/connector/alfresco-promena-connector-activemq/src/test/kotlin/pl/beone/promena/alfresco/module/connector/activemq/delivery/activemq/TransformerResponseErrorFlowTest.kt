package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.alfresco.module.connector.activemq.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.attempt
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.dataDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodeDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodeRefs
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodesChecksum
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformationExecutionResult
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.userName
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import java.time.Duration
import java.time.Duration.ZERO
import java.util.concurrent.TimeoutException

@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
@RunWith(SpringRunner::class)
class TransformerResponseErrorFlowTest {

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Autowired
    private lateinit var promenaMutableTransformationManager: PromenaMutableTransformationManager

    @Autowired
    private lateinit var authorizationService: AuthorizationService

    companion object {
        private val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            null,
            noRetry(),
            dataDescriptor,
            nodesChecksum,
            attempt,
            userName
        )

        private val exception = TransformationException("Exception", TimeoutException::class.java)
    }

    @Before
    fun setUp() {
        clearMocks(authorizationService)
        every { authorizationService.getCurrentUser() } returns userName

        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum // nodesChecksumGenerator.generate(nodeRefs)
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `should receive exception and try to retry again _ first attempt`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                transformationExecutionResult

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(
            transformationExecution.id,
            exception,
            transformationParameters.copy(retry = customRetry(1, Duration.ofMillis(0)))
        )

        shouldThrow<TimeoutException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }
    }

    @Test
    fun `should receive exception, detect difference between nodes checksums and throw NodesInconsistencyException`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                "not equal"  // nodesChecksumGenerator.generate(nodeRefs)

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(transformationExecution.id, exception, transformationParameters)

        shouldThrow<NodesInconsistencyException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "Nodes <$nodeRefs> have changed in the meantime (old checksum <$nodesChecksum>, current checksum <not equal>)"
    }

    @Test
    fun `should receive exception and complete transaction _ last attempt`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(
            transformationExecution.id,
            exception,
            transformationParameters.copy(retry = customRetry(3, ZERO), attempt = 3)
        )

        shouldThrow<TransformationException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "Exception"
    }

    @Test
    fun `should receive exception, complete transaction _ last attempt because of no retry policy`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(
            transformationExecution.id,
            exception,
            transformationParameters.copy(retry = noRetry())
        )

        shouldThrow<TransformationException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "Exception"
    }

    @Test
    fun `should do nothing _ number of max attempts has exceeded`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(
            transformationExecution.id,
            exception,
            transformationParameters.copy(retry = customRetry(1, ZERO), attempt = 2)
        )

        shouldThrow<TimeoutException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }
    }

    @Test
    fun `should receive exception and throw RuntimeException during processing result`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThenThrows  // nodesChecksumGenerator.generate(nodeRefs)
                RuntimeException("exception")

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(
            transformationExecution.id,
            exception,
            transformationParameters.copy(retry = customRetry(1, Duration.ofMillis(0)))
        )

        shouldThrow<RuntimeException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "exception"
    }

    @Test
    fun `should receive exception, throw RuntimeException during processing exception and stop further retrying`() {
        val runtimeException = RuntimeException("exception")

        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThenThrows  // nodesExistenceVerifier.verify(nodeRefs)
                runtimeException // nodesChecksumGenerator.generate(nodeRefs)

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseErrorMessage(transformationExecution.id, exception, transformationParameters)

        shouldThrow<RuntimeException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe runtimeException.message
    }
}