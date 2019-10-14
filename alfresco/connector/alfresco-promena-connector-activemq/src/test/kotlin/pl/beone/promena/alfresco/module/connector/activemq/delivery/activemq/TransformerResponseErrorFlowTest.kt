package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
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
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.TransformationParameters
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

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
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    companion object {
        private val nodeDescriptors = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
        )
        private val nodeRefs = nodeDescriptors.toNodeRefs()
        private const val nodesChecksum = "123456789"
        private const val attempt: Long = 0
        private const val userName = "admin"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
        private val exception = TransformationException(transformation, "Exception")
    }

    @Autowired
    private lateinit var authorizationService: AuthorizationService

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
    fun `should receive exception and try to retry again _ first attempt`() {
        val id = UUID.randomUUID().toString()

        every {
            nodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        every {
            authorizationService.runAs<Mono<List<NodeRef>>>(userName, any())
        } returns Mono.error(exception)

        val transformation = reactiveTransformationManager.startTransformation(id)
        jmsUtils.sendResponseErrorMessage(
            id,
            exception,
            TransformationParameters(
                nodeDescriptors,
                nodesChecksum,
                customRetry(1, Duration.ofMillis(0)),
                attempt,
                userName
            )
        )

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofSeconds(2))
        }.message shouldContain "Timeout on blocking read for"
    }

    @Test
    fun `should receive exception, complete transaction _ last attempt`() {
        val id = UUID.randomUUID().toString()

        every {
            nodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        val transformation = reactiveTransformationManager.startTransformation(id)
        jmsUtils.sendResponseErrorMessage(
            id,
            exception,
            TransformationParameters(
                nodeDescriptors,
                nodesChecksum,
                customRetry(0, Duration.ZERO),
                attempt,
                userName
            )
        )

        shouldThrow<TransformationException> {
            transformation.block(Duration.ofSeconds(1))
        }.message shouldBe exception.message
    }
}