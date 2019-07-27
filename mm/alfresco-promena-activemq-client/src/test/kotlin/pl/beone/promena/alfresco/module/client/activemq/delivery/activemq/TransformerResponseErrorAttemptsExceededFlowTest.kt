package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQBytesMessage
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
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.time.Duration
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-retry-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorAttemptsExceededFlowTest {

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var jmsQueueUtils: JmsQueueUtils

    @Value("\${promena.client.activemq.consumer.queue.response.error}")
    private lateinit var queueResponseError: String

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    companion object {
        private val id = UUID.randomUUID().toString()

        private val nodeRefs = listOf(
            NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
            NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
        )
        private const val nodesChecksum = "123456789"
        private val transformation = singleTransformation("transformer-test", MediaTypeConstants.APPLICATION_PDF, emptyParameters())
        private val exception = TransformationException(
            singleTransformation("transformer-test", MediaTypeConstants.APPLICATION_PDF, emptyParameters() + ("key" to "value")),
            "Exception"
        )
        private val retry = customRetry(1, Duration.ofMillis(100))
        private const val attempt = 1L
    }

    @After
    fun tearDown() {
        jmsQueueUtils.dequeueQueue(queueResponseError)
    }

    @Test
    fun `shouldn't receive any message because the number of attempts has been exceeded`() {
        val transformation = reactiveTransformationManager.startTransformation(id)

        sendResponseErrorMessage()

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofSeconds(1))
        }.message shouldContain "Timeout on blocking read for"

        (jmsTemplate.receive(queueResponseError) as ActiveMQBytesMessage).properties[PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT] shouldBe 1
    }

    private fun sendResponseErrorMessage() {
        jmsTemplate.convertAndSend(
            ActiveMQQueue(queueResponseError),
            exception
        ) { message ->
            message.apply {
                jmsCorrelationID =
                    id
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