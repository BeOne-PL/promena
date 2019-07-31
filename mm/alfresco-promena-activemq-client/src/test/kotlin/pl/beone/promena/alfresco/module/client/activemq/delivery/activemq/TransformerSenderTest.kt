package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import io.kotlintest.fail
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQMessage
import org.junit.After
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
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.net.URI
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
    private lateinit var jmsQueueUtils: JmsQueueUtils

    @Value("\${promena.client.activemq.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var transformerSender: TransformerSender

    companion object {
        private val id = UUID.randomUUID().toString()
        private const val communicationId = File
        private val communicationLocation = URI("file:/tmp")
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"))
        private const val nodesChecksum = "123456789"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val dataDescriptors = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
        private val transformationDescriptor = TransformationDescriptor.of(transformation, dataDescriptors)
        private const val attempt = 1L
    }

    @After
    fun tearDown() {
        jmsQueueUtils.dequeueQueue(queueRequest)
    }

    @Test
    fun `should send message with no retry policy to queue`() {
        transformerSender.send(id, transformationDescriptor, nodeRefs, nodesChecksum, noRetry(), 1)

        validateHeaders(
            mapOf(
                PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID to communicationId.toUTF8Buffer(),
                PromenaAlfrescoJmsHeaders.COMMUNICATION_PARAMETERS_LOCATION to communicationLocation.toString().toUTF8Buffer(),

                PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS to nodeRefs.map { it.toString() },
                PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM to nodesChecksum.toUTF8Buffer(),

                PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT to attempt,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_ENABLED to false,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS to 0L,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY to Duration.ZERO.toString().toUTF8Buffer()
            )
        )
        validateContent()
    }

    @Test
    fun `should send message with custom retry policy to queue`() {
        val retryMaxAttempts = 3L
        val retryNextAttemptDelay = Duration.ofMillis(1500)

        transformerSender.send(id, transformationDescriptor, nodeRefs, nodesChecksum, customRetry(retryMaxAttempts, retryNextAttemptDelay), attempt)

        validateHeaders(
            mapOf(
                PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID to communicationId.toUTF8Buffer(),
                PromenaAlfrescoJmsHeaders.COMMUNICATION_PARAMETERS_LOCATION to communicationLocation.toString().toUTF8Buffer(),

                PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS to nodeRefs.map { it.toString() },
                PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM to nodesChecksum.toUTF8Buffer(),

                PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT to attempt,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_ENABLED to true,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS to retryMaxAttempts,
                PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY to retryNextAttemptDelay.toString().toUTF8Buffer()
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

}