package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.specs.StringSpec
import io.kotlintest.spring.SpringListener
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.assertj.core.api.Assertions.fail
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.FixedHostPortGenericContainer
import pl.beone.promena.alfresco.module.client.messagebroker.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
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
        ContextConfiguration(classes = [GlobalPropertiesContext::class]),
        ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseFlowTest : StringSpec() {

    override fun listeners() = listOf(SpringListener)

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.response}")
    private lateinit var queueResponse: String

    @Autowired
    private lateinit var completedTransformationManager: CompletedTransformationManager

    @Autowired
    private lateinit var alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver

    override fun beforeSpec(spec: Spec) {
        FixedHostPortGenericContainer<Nothing>("rmohr/activemq:5.15.6-alpine").apply {
            withFixedExposedPort(61616, 61616)
            start()
        }
    }

    companion object {
        private val transformedDataDescriptors = listOf(
                TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata(mapOf("key" to "value")))
        )
    }

    init {
        "should receive transformed data from response queue and save it in ACS" {
            val id = UUID.randomUUID().toString()
            every {
                alfrescoTransformedDataDescriptorSaver.save("transformer-test",
                                                            listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                                                   NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")),
                                                            MediaTypeConstants.TEXT_PLAIN,
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
    }

    private fun sendResponseMessage(correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponse), transformedDataDescriptors) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, "transformer-test")

                setLongProperty(PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP,
                                System.currentTimeMillis() + Duration.ofDays(1).toMillis())

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