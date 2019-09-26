package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

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
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorRetryFlowTest {

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Autowired
    private lateinit var alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    companion object {
        private val id = UUID.randomUUID().toString()
        private val nodeDescriptors = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
        )
        private val nodeRefs = nodeDescriptors.toNodeRefs()
        private const val nodesChecksum = "123456789"
        private val retry = customRetry(1, Duration.ofMillis(100))
        private const val userName = "admin"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val exception = TransformationTerminationException(transformation, "Exception")
    }

    @Autowired
    private lateinit var alfrescoAuthenticationService: AlfrescoAuthenticationService

    @Before
    fun setUp() {
        clearMocks(alfrescoAuthenticationService)
        every { alfrescoAuthenticationService.getCurrentUser() } returns userName
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `should receive exception and throw it after 1 attempt`() {
        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        every {
            alfrescoAuthenticationService.runAs<Mono<List<NodeRef>>>(userName, any())
        } returns Mono.error(exception)

        val transformation = reactiveTransformationManager.startTransformation(id)

        jmsUtils.sendResponseErrorMessage(
            id,
            exception,
            TransformationParameters(
                nodeDescriptors,
                nodesChecksum,
                retry,
                0,
                userName
            )
        )
        thread {
            Thread.sleep(1000)
            jmsUtils.sendResponseErrorMessage(
                id,
                exception,
                TransformationParameters(
                    nodeDescriptors,
                    nodesChecksum,
                    retry,
                    1,
                    userName
                )
            )
        }

        shouldThrow<TransformationTerminationException> {
            transformation.block(Duration.ofSeconds(2))
        }.message shouldBe exception.message
    }
}