package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.verify
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
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
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.concurrent.thread

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorRetryFlowTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var jmsQueueUtils: JmsQueueUtils

    @Value("\${promena.client.activemq.consumer.queue.response.error}")
    private lateinit var queueResponseError: String

    @Autowired
    private lateinit var alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    @Autowired
    private lateinit var activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService

    companion object {
        private val id = UUID.randomUUID().toString()
        private val nodeRefs = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        )
        private const val nodesChecksum = "123456789"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val exception = TransformationTerminationException(transformation, "Exception")
        private val retry = customRetry(2, Duration.ofMillis(100))
    }

    @After
    fun tearDown() {
        jmsQueueUtils.dequeueQueue(queueResponseError)
    }

    @Test
    fun `should receive exception and throw it after 1 attempt`() {
        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        val monoError = Mono.error<List<NodeRef>>(exception)
        every {
            activeMQAlfrescoPromenaService.transformAsync(id, transformation, nodeRefs, retry, 1)
        } returns monoError
        every {
            activeMQAlfrescoPromenaService.transformAsync(id, transformation, nodeRefs, retry, 2)
        } returns monoError

        val transformation = reactiveTransformationManager.startTransformation(id)

        sendResponseErrorMessage(0)
        thread {
            Thread.sleep(1000)
            sendResponseErrorMessage(1)
        }

        shouldThrow<TransformationTerminationException> {
            transformation.block(Duration.ofSeconds(2))
        }.message shouldBe exception.message

        verify {
            activeMQAlfrescoPromenaService.transformAsync(id, Companion.transformation, nodeRefs, retry, 1)
        }
    }

    private fun sendResponseErrorMessage(attempt: Int) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponseError), exception) { message ->
            message.apply {
                jmsCorrelationID = id
                setLongProperty(PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP, System.currentTimeMillis())
                setLongProperty(PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP, System.currentTimeMillis() + Duration.ofDays(1).toMillis())

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM, nodesChecksum)

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT, attempt)
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_ENABLED, true)
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS, retry.maxAttempts)
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY, retry.nextAttemptDelay.toString())
            }
        }
    }
}