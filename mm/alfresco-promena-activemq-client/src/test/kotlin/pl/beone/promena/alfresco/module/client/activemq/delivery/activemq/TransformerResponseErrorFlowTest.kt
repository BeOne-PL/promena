package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
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
import pl.beone.promena.alfresco.module.client.activemq.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.client.activemq.applicationmodel.PromenaAlfrescoJmsHeaders
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.client.activemq.external.ActiveMQAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import javax.jms.Message

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorFlowTest {

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
    private lateinit var activeMQAlfrescoPromenaTransformer: ActiveMQAlfrescoPromenaTransformer

    companion object {
        private val nodeRefs = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        )
        private const val nodesChecksum = "123456789"
        private const val userName = "admin"
        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters())
        private val exception = TransformationException(transformation, "Exception")
    }

    @Autowired
    private lateinit var alfrescoAuthenticationService: AlfrescoAuthenticationService

    @Before
    fun setUp() {
        clearMocks(alfrescoAuthenticationService)
        every { alfrescoAuthenticationService.getCurrentUser() } returns userName
    }

    @After
    fun tearDown() {
        jmsQueueUtils.dequeueQueue(queueResponseError)
    }

    @Test
    fun `should receive exception and try to retry again _ first attempt`() {
        val id = UUID.randomUUID().toString()
        val retry = customRetry(1, Duration.ofMillis(0))

        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        every {
            alfrescoAuthenticationService.runAs<Mono<List<NodeRef>>>(userName, any())
        } returns Mono.error(exception)

        val transformation = reactiveTransformationManager.startTransformation(id)
        sendResponseErrorMessage(id, 0, retry)

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofSeconds(2))
        }.message shouldContain "Timeout on blocking read for"
    }

    @Test
    fun `should receive exception, complete transaction _ last attempt`() {
        val id = UUID.randomUUID().toString()

        every {
            alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        } returns nodesChecksum

        val transformation = reactiveTransformationManager.startTransformation(id)
        sendResponseErrorMessage(id, 0, noRetry())

        shouldThrow<TransformationException> {
            transformation.block(Duration.ofSeconds(1))
        }.message shouldBe exception.message
    }

    private fun sendResponseErrorMessage(correlationId: String, attempt: Long, retry: Retry) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueResponseError), exception) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM, nodesChecksum)
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_USER_NAME, userName)

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT, attempt)
                setRetryHeaders(retry)
            }
        }
    }

    private fun Message.setRetryHeaders(retry: Retry) {
        if (retry != noRetry()) {
            setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS, retry.maxAttempts)
            setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY, retry.nextAttemptDelay.toString())
        } else {
            setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS, 0)
            setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY, Duration.ZERO.toString())
        }
    }
}