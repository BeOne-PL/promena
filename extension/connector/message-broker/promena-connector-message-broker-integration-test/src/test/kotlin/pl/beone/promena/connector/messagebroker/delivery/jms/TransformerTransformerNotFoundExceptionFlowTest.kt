package pl.beone.promena.connector.messagebroker.delivery.jms

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import org.apache.activemq.command.ActiveMQBytesMessage
import org.apache.activemq.command.ActiveMQQueue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.connector.messagebroker.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.messagebroker.integrationtest.test.MockContext
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:module-connector-message-broker-test.properties")
class TransformerTransformerNotFoundExceptionFlowTest {

    companion object {
        private val correlationId = UUID.randomUUID().toString()
        private val expectException = TransformerNotFoundException("Transformer not found")
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.connector.message-broker.consumer.queue.request}")
    private lateinit var queueRequest: String

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @Before
    fun mock() {
        mockkObject(transformationUseCase)
    }

    @Test
    fun `send data to transformation request queue _ should throw TransformerNotFoundException and leave message in queue`() {
        every { transformationUseCase.transform(any(), any(), any()) } throws expectException

        sendRequestMessage()
        Thread.sleep(2000)

        jmsTemplate.browse(queueRequest) { session, browser ->
            val messages = browser.enumeration.toList()

            messages shouldHaveSize 1
            (messages.first() as ActiveMQBytesMessage).correlationId shouldBe correlationId

            clearRequestQueue()
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest),
                                   TransformationDescriptor(listOf(DataDescriptor("".toInMemoryData(), TEXT_PLAIN)),
                                                            MediaTypeConstants.APPLICATION_JSON,
                                                            MapParameters.empty())) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, MockContext.transformerId)

                setObjectProperty("send_back_nodeRefs", listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                               "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
                setObjectProperty("send_back_targetMediaType_mimeType", MediaTypeConstants.TEXT_PLAIN.mimeType)
                setObjectProperty("send_back_targetMediaType_charset", MediaTypeConstants.TEXT_PLAIN.charset.toString())
                setObjectProperty("send_back_parameters", MapParameters(mapOf("key" to "value")).getAll())
                setObjectProperty("send_back_timeout", 3000)
            }
        }
    }

    private fun clearRequestQueue() {
        try {
            Executors.newSingleThreadExecutor().submit { jmsTemplate.receive(queueRequest) }
                    .get(2, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            // expected
        }
    }
}