package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.assertj.core.api.Assertions.fail
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
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
        ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
        ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseFlowTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.response}")
    private lateinit var queueResponse: String

    @Autowired
    private lateinit var completedTransformationManager: CompletedTransformationManager

    @Autowired
    private lateinit var alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver

    companion object {
        private val transformedDataDescriptors = listOf(
                TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata(mapOf("key" to "value")))
        )
        private const val transformerId = "transformer-test"
    }

    @Test
    fun `should receive transformed data from response queue and persist it`() {
        val id = UUID.randomUUID().toString()
        every {
            alfrescoTransformedDataDescriptorSaver.save(transformerId,
                                                        listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                                               NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")),
                                                        TEXT_PLAIN,
                                                        transformedDataDescriptors)
        } returns listOf(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))

        completedTransformationManager.startTransformation(id)
        sendResponseMessage(id)

        try {
            completedTransformationManager.getTransformedNodeRefs(id, Duration.ofSeconds(2)) shouldContainExactly
                    listOf(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
        } catch (e: TimeoutException) {
            fail("Waiting time for transformation passed. Check logs for more details")
        }
    }

    private fun sendResponseMessage(correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponse), transformedDataDescriptors) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PROMENA_TRANSFORMER_ID, transformerId)

                setLongProperty(PROMENA_TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PROMENA_TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis() + Duration.ofDays(1).toMillis())

                setObjectProperty(SEND_BACK_NODE_REFS, listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                              "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, TEXT_PLAIN.mimeType)
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, TEXT_PLAIN.charset.toString())
                setObjectProperty(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, MapParameters(mapOf("key" to "value")).getAll())
            }
        }
    }
}