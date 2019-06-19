package pl.beone.promena.connector.messagebroker.delivery.jms

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeInRange
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import org.apache.activemq.command.ActiveMQQueue
import org.junit.Before
import org.junit.ClassRule
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
import org.testcontainers.containers.FixedHostPortGenericContainer
import pl.beone.promena.connector.messagebroker.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.messagebroker.integrationtest.test.TransformerResponseConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:module-connector-activemq-test.properties")
class TransformerIncorrectFlowTest {

    companion object {
        @get:ClassRule
        @JvmStatic
        val activemq = FixedHostPortGenericContainer<Nothing>("rmohr/activemq:5.15.6-alpine").apply {
            withFixedExposedPort(61616, 61616)
            start()
        }

        private const val transformerId = "test-transformer"
        private val correlationId = UUID.randomUUID().toString()
        private val expectException = TransformerTimeoutException("Time expired")
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.connector.activemq.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var transformerResponseConsumer: TransformerResponseConsumer

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @Before
    fun mock() {
        mockkObject(transformationUseCase)
    }

    @Test
    fun `send data to transformation request queue _ should handle exception to response error queue`() {
        every { transformationUseCase.transform(any(), any(), any()) } throws expectException

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
                                      PromenaJmsHeader.PROMENA_TRANSFORMER_ID to transformerId,
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

        exception.let {
            it should beInstanceOf(expectException::class)
            it.message shouldBe expectException.message
            it.localizedMessage shouldBe expectException.localizedMessage
            it.cause shouldBe expectException.cause
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest),
                                   TransformationDescriptor(listOf(DataDescriptor("".toInMemoryData(), TEXT_PLAIN)),
                                                            MediaTypeConstants.APPLICATION_JSON,
                                                            MapParameters.empty())) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

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