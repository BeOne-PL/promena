package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.fail
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQMessage
import org.fusesource.hawtbuf.UTF8Buffer
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.messagebroker.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.net.URI
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-sender-test.properties"])
@ContextHierarchy(
        ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
        ContextConfiguration(classes = [SetupContext::class])
)
class TransformerSenderTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var transformerSender: TransformerSender

    companion object {
        private val dataDescriptors = listOf(DataDescriptor(MemoryData("test".toByteArray()), TEXT_PLAIN, MapMetadata(mapOf("key" to "value"))))
        private val id = UUID.randomUUID().toString()
        private const val communicationId = File
        private val communicationLocation = URI("file:/tmp")
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"))
        private const val nodesChecksum = "123456789"
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        private val parameters = MapParameters(mapOf("key" to "value"))
        private const val attempt = 1L
        private val transformationDescriptor = TransformationDescriptor(dataDescriptors, targetMediaType, parameters)
    }

    @Test
    fun `should send transformation descriptor data to queue`() {
        transformerSender.send(dataDescriptors,
                               id,
                               "transformer-test",
                               nodeRefs,
                               nodesChecksum,
                               targetMediaType,
                               parameters,
                               attempt)

        validateHeaders()
        validateContent()
    }

    private fun validateHeaders() {
        jmsTemplate.browse(queueRequest) { _, queueBrowser ->
            val messages = queueBrowser.enumeration.asIterator().asSequence().toList()

            if (messages.isEmpty()) {
                fail("No messages available in queue")
            }

            val activeMQMessage = messages.first() as ActiveMQMessage
            activeMQMessage.jmsCorrelationID shouldBe id
            activeMQMessage.properties shouldContainAll
                    mapOf(PromenaJmsHeader.SEND_BACK_NODE_REFS to nodeRefs.map { it.toString() },
                          PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM to UTF8Buffer(nodesChecksum),
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE to UTF8Buffer(targetMediaType.mimeType),
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET to UTF8Buffer(targetMediaType.charset.name()),
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS to parameters.getAll(),
                          PromenaJmsHeader.SEND_BACK_ATTEMPT to attempt,
                          PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION to UTF8Buffer(communicationId),
                          PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION to UTF8Buffer(communicationLocation.toString()))
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