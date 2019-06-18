package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.verify
import org.alfresco.service.cmr.repository.NodeRef
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
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-retry-test.properties"])
@ContextHierarchy(
        ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
        ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorRetryFlowTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.client.message-broker.consumer.queue.response.error}")
    private lateinit var queueResponseError: String

    @Autowired
    private lateinit var alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    @Autowired
    private lateinit var activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService

    companion object {
        private val id = UUID.randomUUID().toString()

        private val exception = RuntimeException("Exception")
        private const val transformerId = "transformer-test"
        private val nodeRefs = listOf(NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                                      NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
        private const val nodesChecksum = "123456789"
        private val parameters = MapParameters(mapOf("key" to "value"))
    }

    @Test
    fun `should receive exception and throw it after 1 attempt`() {
        every {
            alfrescoNodesChecksumGenerator.generateChecksum(TransformerResponseFlowTest.nodeRefs)
        } returns nodesChecksum

        val monoError = Mono.error<List<NodeRef>>(exception)
        every {
            activeMQAlfrescoPromenaService.transformAsync(transformerId, nodeRefs, TEXT_PLAIN, parameters)
        } returns monoError
        every {
            activeMQAlfrescoPromenaService.transformAsync(id, transformerId, nodeRefs, TEXT_PLAIN, parameters, 1)
        } returns monoError

        val transformation = reactiveTransformationManager.startTransformation(id)

        sendResponseErrorMessage(0)
        thread {
            Thread.sleep(500)
            sendResponseErrorMessage(1)
        }

        shouldThrow<RuntimeException> {
            transformation.block(Duration.ofSeconds(2))
        }.apply {
            message shouldBe exception.message
        }

        verify { activeMQAlfrescoPromenaService.transformAsync(id, transformerId, nodeRefs, TEXT_PLAIN, parameters, 1) }
    }

    private fun sendResponseErrorMessage(attempt: Int) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponseError), exception) { message ->
            message.apply {
                jmsCorrelationID = id
                setStringProperty(PROMENA_TRANSFORMER_ID, transformerId)

                setLongProperty(PROMENA_TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PROMENA_TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis() + Duration.ofDays(1).toMillis())

                setObjectProperty(SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(SEND_BACK_NODES_CHECKSUM, nodesChecksum)
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, TEXT_PLAIN.mimeType)
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, TEXT_PLAIN.charset.toString())
                setObjectProperty(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, parameters.getAll())
                setObjectProperty(SEND_BACK_ATTEMPT, attempt)
            }
        }
    }
}