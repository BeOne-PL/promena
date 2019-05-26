package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
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
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
        ContextConfiguration(classes = [GlobalPropertiesContext::class]),
        ContextConfiguration(classes = [SetupContext::class])
)
class TransformerCorrectFlowTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.response}")
    private lateinit var queueResponse: String

    @Test
    fun name() {
        val id = UUID.randomUUID().toString()

        sendResponseMessage(TransformedDataDescriptor(InMemoryData("test".toByteArray()),
                                                      MapMetadata(mapOf("key" to "value"))),
                            id)

        Thread.sleep(3000)
    }

    private fun sendResponseMessage(transformedDataDescriptor: TransformedDataDescriptor,
                                    correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponse), listOf(transformedDataDescriptor)) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, "transformer-test")

                setLongProperty(PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis())

                setObjectProperty(PromenaJmsHeader.SEND_BACK_NODE_REFS,
                                  listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                         "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, MediaTypeConstants.TEXT_PLAIN.mimeType)
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, MediaTypeConstants.TEXT_PLAIN.charset.toString())
                setObjectProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, MapParameters(mapOf("key" to "value")).getAll())
            }
        }
    }
}