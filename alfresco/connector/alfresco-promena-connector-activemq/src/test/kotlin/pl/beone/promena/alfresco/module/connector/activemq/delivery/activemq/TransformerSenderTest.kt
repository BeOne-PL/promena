package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.fail
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQMessage
import org.fusesource.hawtbuf.UTF8Buffer
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
import pl.beone.promena.alfresco.module.connector.activemq.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS_STRING
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.TransformationParameters
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.time.Duration
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerSenderTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Value("\${promena.connector.activemq.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var transformerSender: TransformerSender

    companion object {
        private val id = UUID.randomUUID().toString()
        private val nodeDescriptors = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
        )
        private const val nodesChecksum = "123456789"
        private val retry = customRetry(3, Duration.ofMillis(1000))
        private const val attempt: Long = 0
        private const val userName = "admin"
        private val transformationParameters = TransformationParameters(
            nodeDescriptors,
            nodesChecksum,
            retry,
            attempt,
            userName
        )
        private val transformationDescriptor = transformationDescriptor(
            singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value")),
            singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value")),
            memoryCommunicationParameters()
        )
    }

    @Autowired
    private lateinit var authorizationService: AuthorizationService

    @Autowired
    private lateinit var transformationParametersSerializationService: TransformationParametersSerializationService

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
    fun `should send message to queue`() {
        transformerSender.send(
            id,
            transformationDescriptor,
            nodeDescriptors,
            nodesChecksum,
            retry,
            attempt
        )

        validateHeaders(
            mapOf(
                SEND_BACK_TRANSFORMATION_PARAMETERS to transformationParametersSerializationService.serialize(transformationParameters).toUTF8Buffer(),
                SEND_BACK_TRANSFORMATION_PARAMETERS_STRING to transformationParameters.toString().toUTF8Buffer(),
                SEND_BACK_ATTEMPT to attempt,
                SEND_BACK_RETRY_MAX_ATTEMPTS to retry.maxAttempts
            )
        )
        validateContent()
    }

    private fun validateHeaders(headers: Map<String, Any>) {
        jmsTemplate.browse(queueRequest) { _, queueBrowser ->
            val messages = queueBrowser.enumeration.asIterator().asSequence().toList()

            if (messages.isEmpty()) {
                fail("No messages available in queue")
            }

            messages.first().let { message ->
                val activeMQMessage = message as ActiveMQMessage
                activeMQMessage.jmsCorrelationID shouldBe id
                activeMQMessage.properties shouldContainAll headers
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun validateContent() {
        try {
            jmsTemplate.receiveAndConvert(queueRequest) as TransformationDescriptor shouldBe transformationDescriptor
        } catch (e: Exception) {
            throw AssertionError("Message should be type of <TransformationDescriptor>", e)
        }
    }

    private fun String.toUTF8Buffer(): UTF8Buffer =
        UTF8Buffer(this)
}