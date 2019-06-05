package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQMessage
import org.assertj.core.api.Assertions.fail
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
import pl.beone.promena.alfresco.module.client.messagebroker.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
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

    @Value("\${promena.client.message-broker.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var transformerSender: TransformerSender

    @Test
    fun `should send transformation descriptor data to queue`() {
        val dataDescriptors = listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN))
        val id = UUID.randomUUID().toString()
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"))
        val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        val parameters = MapParameters(mapOf("key" to "value"))
        val transformationDescriptor = TransformationDescriptor(dataDescriptors, targetMediaType, parameters)

        transformerSender.send(dataDescriptors,
                               id,
                               "transformer-test",
                               nodeRefs,
                               targetMediaType,
                               parameters)

        validateHeaders(id, nodeRefs, targetMediaType, parameters)
        validateContent(transformationDescriptor)
    }

    private fun validateHeaders(id: String, nodeRefs: List<NodeRef>, targetMediaType: MediaType, parameters: MapParameters) {
        jmsTemplate.browse(queueRequest) { _, queueBrowser ->
            val messages = queueBrowser.enumeration.asIterator().asSequence().toList()

            if (messages.isEmpty()) {
                fail("No messages available in queue")
            }

            val activeMQMessage = messages.first() as ActiveMQMessage
            activeMQMessage.jmsCorrelationID shouldBe id
            activeMQMessage.properties shouldContainAll
                    mapOf(PromenaJmsHeader.SEND_BACK_NODE_REFS to nodeRefs.map { it.toString() },
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE to UTF8Buffer(targetMediaType.mimeType),
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET to UTF8Buffer(targetMediaType.charset.name()),
                          PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS to parameters.getAll())
            activeMQMessage.properties shouldNotContainKey PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun validateContent(transformationDescriptor: TransformationDescriptor) {
        try {
            jmsTemplate.receiveAndConvert(queueRequest) as TransformationDescriptor shouldBe transformationDescriptor
        } catch (e: Exception) {
            fail("Message should be type of <TransformationDescriptor>", e)
        }
    }

}