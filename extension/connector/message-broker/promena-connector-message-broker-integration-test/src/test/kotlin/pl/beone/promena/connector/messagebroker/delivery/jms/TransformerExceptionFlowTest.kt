package pl.beone.promena.connector.messagebroker.delivery.jms

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.numerics.*
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkObject
import org.apache.activemq.command.ActiveMQQueue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.connector.messagebroker.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.messagebroker.integrationtest.test.MockContext
import pl.beone.promena.connector.messagebroker.integrationtest.test.QueueClearer
import pl.beone.promena.connector.messagebroker.integrationtest.test.TransformerResponseConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.*
import javax.jms.Message

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:module-connector-message-broker-test.properties")
class TransformerExceptionFlowTest {

    companion object {
        private val correlationId = UUID.randomUUID().toString()
        private val expectException = TransformerTimeoutException("Time expired")
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.connector.message-broker.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var queueClearer: QueueClearer

    @Autowired
    private lateinit var transformerResponseConsumer: TransformerResponseConsumer

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @Before
    fun setUp() {
        mockkObject(transformationUseCase)
        clearMocks(transformationUseCase)

        queueClearer.clearQueues()
    }

    @Test
    fun `send data to transformation request queue _ should handle exception to response error queue`() {
        every { transformationUseCase.transform(any(), any(), any()) } answers {
            Thread.sleep(300)
            throw expectException
        }

        val startTimestamp = getTimestamp()
        sendRequestMessage()
        val (headers, exception) = try {
            transformerResponseConsumer.getErrorMessage(3000)
        } catch (e: IllegalStateException) {
            throw AssertionError("Couldn't get message from response error queue")
        }
        val endTimestamp = getTimestamp()

        headers.let {
            it shouldContainAll mapOf(CORRELATION_ID to correlationId,
                                      PromenaJmsHeader.PROMENA_TRANSFORMER_ID to MockContext.transformerId,
                                      "send_back_nodeRefs" to listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                                     "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"),
                                      "send_back_targetMediaType_mimeType" to TEXT_PLAIN.mimeType,
                                      "send_back_targetMediaType_charset" to TEXT_PLAIN.charset.toString(),
                                      "send_back_parameters" to MapParameters(mapOf("key" to "value")).getAll(),
                                      "send_back_timeout" to 3000)
            it shouldContainKey PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP
            it shouldContainKey PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP
        }

        val transformationStartTimestamp = headers[PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP] as Long
        val transformationEndTimestamp = headers[PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP] as Long

        transformationStartTimestamp.let {
            it.shouldBeInRange(startTimestamp..endTimestamp)
            it shouldBeLessThan transformationEndTimestamp
        }

        transformationEndTimestamp.let {
            it.shouldBeInRange(startTimestamp..endTimestamp)
            it shouldBeGreaterThan transformationStartTimestamp
        }

        (transformationEndTimestamp - transformationStartTimestamp) shouldBeGreaterThanOrEqual 300

        exception.let {
            it should beInstanceOf(expectException::class)
            it.message shouldBe expectException.message
            it.localizedMessage shouldBe expectException.localizedMessage
            it.cause shouldBe expectException.cause
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest),
                                   TransformationDescriptor(listOf(DataDescriptor("".toInMemoryData(), TEXT_PLAIN, MapMetadata.empty())),
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
}