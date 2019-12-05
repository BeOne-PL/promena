package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import org.alfresco.service.cmr.repository.InvalidNodeRefException
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
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.retry
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformationExecutionResult
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.userName
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodesInconsistencyException
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
@RunWith(SpringRunner::class)
class TransformerResponseFlowTest {

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
            retry,
            dataDescriptor,
            nodesChecksum,
            attempt,
            userName
        )

        private val performedTransformationDescriptor = performedTransformationDescriptor(
            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value"))
        )
    }

    @Before
    fun setUp() {
        clearMocks(authorizationService)
        every { authorizationService.getCurrentUser() } returns userName
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `should receive message from response queue and go correct path`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThen // nodesChecksumGenerator.generate(nodeRefs)
                transformationExecutionResult

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseMessage(transformationExecution.id, performedTransformationDescriptor, transformationParameters)

        promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2)) shouldBe transformationExecutionResult
    }

    @Test
    fun `should detect difference between nodes checksums and throw NodesInconsistencyException`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                "not equal"  // nodesChecksumGenerator.generate(nodeRefs)

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseMessage(transformationExecution.id, performedTransformationDescriptor, transformationParameters)

        shouldThrow<NodesInconsistencyException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "Nodes <$nodeRefs> have changed in the meantime (old checksum <$nodesChecksum>, current checksum <not equal>)"
    }

    @Test
    fun `should detect that one of nodes doesn't exist and throw InvalidNodeRefException`() {
        every { authorizationService.runAs<Any>(userName, any()) } throws
                InvalidNodeRefException("Node <${nodeRefs[0]}> doesn't exist", nodeRefs[0])

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseMessage(transformationExecution.id, performedTransformationDescriptor, transformationParameters)

        shouldThrow<InvalidNodeRefException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "Node <${nodeRefs[0]}> doesn't exist"
    }

    @Test
    fun `should throw RuntimeException during processing result`() {
        every { authorizationService.runAs<Any>(userName, any()) } returns
                Unit andThen // nodesExistenceVerifier.verify(nodeRefs)
                nodesChecksum andThenThrows  // nodesChecksumGenerator.generate(nodeRefs)
                RuntimeException("exception")

        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        jmsUtils.sendResponseMessage(transformationExecution.id, performedTransformationDescriptor, transformationParameters)

        shouldThrow<RuntimeException> {
            promenaMutableTransformationManager.getResult(transformationExecution, Duration.ofSeconds(2))
        }.message shouldBe "exception"
    }
}