package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
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
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.node.plus
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.PostTransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.node.NodesExistenceVerifier
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.time.Duration
import java.time.Duration.ZERO
import java.util.concurrent.TimeoutException

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorFlowTest {

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Autowired
    private lateinit var nodesChecksumGenerator: NodesChecksumGenerator

    @Autowired
    private lateinit var nodesExistenceVerifier: NodesExistenceVerifier

    @Autowired
    private lateinit var promenaMutableTransformationManager: PromenaMutableTransformationManager

    @Autowired
    private lateinit var authorizationService: AuthorizationService

    companion object {
        val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
        private val nodeDescriptor =
            NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata() + ("key" to "value")) +
                    NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toSingleNodeDescriptor(emptyMetadata())
        private val nodeRefs = nodeDescriptor.toNodeRefs()
        private const val nodesChecksum = "123456789"
        private const val userName = "admin"
        private val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            PostTransformationExecution { _, _, _, _ -> },
            noRetry(),
            singleDataDescriptor("".toMemoryData(), APPLICATION_PDF, emptyMetadata()),
            nodesChecksum,
            0,
            userName
        )

        private val transformationExecutionResult = transformationExecutionResult(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
        private val exception = TransformationException("Exception", TimeoutException::class.java)
    }

    @Before
    fun setUp() {
        clearMocks(authorizationService)
        every { authorizationService.getCurrentUser() } returns userName

        clearMocks(nodesChecksumGenerator)
        every { nodesChecksumGenerator.generate(nodeRefs) } returns nodesChecksum

        clearMocks(nodesExistenceVerifier)
        every { nodesExistenceVerifier.verify(nodeRefs) } just Runs
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `should receive exception and try to retry again _ first attempt`() {
        every { authorizationService.runAs<TransformationExecutionResult>(userName, any()) } returns transformationExecutionResult

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
    fun `should receive exception _ throw RuntimeException during processing result`() {
        every { authorizationService.runAs<TransformationExecutionResult>(userName, any()) } throws RuntimeException("exception")

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
}