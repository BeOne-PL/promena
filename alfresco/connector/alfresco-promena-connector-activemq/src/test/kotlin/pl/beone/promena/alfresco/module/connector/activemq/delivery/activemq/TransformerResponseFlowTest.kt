package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.matchers.collections.shouldContainExactly
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
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.time.Duration
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseFlowTest {

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Autowired
    private lateinit var alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    @Autowired
    private lateinit var alfrescoAuthenticationService: AlfrescoAuthenticationService

    companion object {
        private val nodeDescriptors = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
        )
        private val nodeRefs = nodeDescriptors.toNodeRefs()
        private const val nodesChecksum = "123456789"
        private const val userName = "admin"
        private val transformationParameters = TransformationParameters(
            nodeDescriptors,
            nodesChecksum,
            customRetry(1, Duration.ZERO),
            0,
            userName
        )
        private val performedTransformationDescriptor = performedTransformationDescriptor(
            singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters()),
            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value"))
        )
        private val resultNodeRefs = listOf(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
    }

    @Before
    fun setUp() {
        clearMocks(alfrescoAuthenticationService)
        every { alfrescoAuthenticationService.getCurrentUser() } returns userName
        every { alfrescoAuthenticationService.runAs<List<NodeRef>>(userName, any()) } returns resultNodeRefs
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `should receive message from response queue and persist it`() {
        val id = UUID.randomUUID().toString()

        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        val transformation = reactiveTransformationManager.startTransformation(id)
        jmsUtils.sendResponseMessage(
            id,
            performedTransformationDescriptor,
            transformationParameters
        )

        transformation.block(Duration.ofSeconds(2)) shouldContainExactly
                resultNodeRefs
    }

    @Test
    fun `should detect the difference between nodes checksums and throw AnotherTransformationIsInProgressException`() {
        val id = UUID.randomUUID().toString()

        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns "not equal"

        val transformation = reactiveTransformationManager.startTransformation(id)
        jmsUtils.sendResponseMessage(
            id,
            performedTransformationDescriptor,
            transformationParameters
        )

        shouldThrow<AnotherTransformationIsInProgressException> {
            transformation.block(Duration.ofSeconds(2))
        }
    }
}