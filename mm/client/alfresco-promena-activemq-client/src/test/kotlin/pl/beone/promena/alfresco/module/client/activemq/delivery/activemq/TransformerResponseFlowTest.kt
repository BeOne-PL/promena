package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.alfresco.module.client.activemq.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.activemq.applicationmodel.PromenaAlfrescoJmsHeaders
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
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
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var jmsQueueUtils: JmsQueueUtils

    @Value("\${promena.client.activemq.consumer.queue.response}")
    private lateinit var queueResponse: String

    @Autowired
    private lateinit var alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    @Autowired
    private lateinit var alfrescoAuthenticationService: AlfrescoAuthenticationService

    companion object {
        private val nodeRefs = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        )
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
        private val transformedDataDescriptor = singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value"))
        private val performedTransformationDescriptor = performedTransformationDescriptor(transformation, transformedDataDescriptor)
        private const val nodesChecksum = "123456789"
        private const val userName = "admin"
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
        jmsQueueUtils.dequeueQueue(queueResponse)
    }

    @Test
    fun `should receive message from response queue and persist it`() {
        val id = UUID.randomUUID().toString()

        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        val transformation = reactiveTransformationManager.startTransformation(id)
        sendResponseMessage(id)

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
        sendResponseMessage(id)

        shouldThrow<AnotherTransformationIsInProgressException> {
            transformation.block(Duration.ofSeconds(2))
        }
    }

    private fun sendResponseMessage(correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponse), performedTransformationDescriptor) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setLongProperty(PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis() + Duration.ofDays(1).toMillis())

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setStringProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM, nodesChecksum)
                setStringProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_USER_NAME, userName)
            }
        }
    }

}