package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.properties.verifyNone
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.kotlintest.spring.SpringListener
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.delay
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.assertj.core.api.Assertions
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
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
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
class TransformerResponseErrorFlowTest : StringSpec() {

    override fun listeners() = listOf(SpringListener)

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.response.error}")
    private lateinit var queueResponseError: String

    @Autowired
    private lateinit var completedTransformationManager: CompletedTransformationManager

    @Autowired
    private lateinit var activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService

    companion object {
        private val exception = RuntimeException("Exception")
        private const val transformerId = "transformer-test"
    }

    init {
        "should receive transformed data from response queue and save it in ACS" {
            val id = UUID.randomUUID().toString()
            every {
                activeMQAlfrescoPromenaService.transformAsync(transformerId,
                                                              listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                                                     NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")),
                                                              MediaTypeConstants.TEXT_PLAIN,
                                                              MapParameters(mapOf("key" to "value")))
            } just Runs

            completedTransformationManager.startTransformation(id)
            sendResponseErrorMessage(id)

            shouldThrow<RuntimeException> {
                completedTransformationManager.getTransformedNodeRefs(id, Duration.ofSeconds(2))
            }.apply {
                message shouldBe "Exception"
            }

            verify(exactly = 0) {
                activeMQAlfrescoPromenaService.transformAsync(transformerId,
                                                              listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                                                     NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")),
                                                              MediaTypeConstants.TEXT_PLAIN,
                                                              MapParameters(mapOf("key" to "value")))
            }
            delay(120)
            verify(exactly = 1) {
                activeMQAlfrescoPromenaService.transformAsync(transformerId,
                                                              listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                                                     NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")),
                                                              MediaTypeConstants.TEXT_PLAIN,
                                                              MapParameters(mapOf("key" to "value")))
            }
        }
    }

    private fun sendResponseErrorMessage(correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponseError), exception) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

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