package pl.beone.promena.connector.activemq.delivery.jms

import io.mockk.every
import io.mockk.mockkObject
import org.apache.activemq.command.ActiveMQQueue
import org.assertj.core.api.Assertions.assertThat
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
import pl.beone.promena.connector.activemq.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.activemq.integrationtest.test.TransformerResponseConsumer
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
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${activemq.promena.consumer.queue.request}")
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
        //
        val transformerId = "test-transformer"
        val correlationId = UUID.randomUUID().toString()
        val expectException = TransformerTimeoutException("Time expired")

        every { transformationUseCase.transform(any(), any<TransformationDescriptor>(), any()) } throws expectException

        //
        val startTimestamp = getTimestamp()
        sendRequestMessage(transformerId, correlationId)
        val (headers, exception) = transformerResponseConsumer.getErrorMessage(3000)
        val endTimestamp = getTimestamp()

        //
        assertThat(headers)
                .containsEntry(CORRELATION_ID, correlationId)
                .containsEntry(TransformerJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)
                .containsEntry("send_back_alf_node", listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                            "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
                .containsEntry("send_back_targetMediaType_mimeType", TEXT_PLAIN.mimeType)
                .containsEntry("send_back_targetMediaType_charset", TEXT_PLAIN.charset.toString())
                .containsEntry("send_back_parameters", MapParameters(mapOf("key" to "value")).getAll())
                .containsEntry("send_back_timeout", 3000)
                .containsKey(TransformerJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP)
                .containsKey(TransformerJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP)

        val transformationStartTimestamp = headers[TransformerJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP] as Long
        val transformationEndTimestamp = headers[TransformerJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP] as Long
        assertThat(transformationStartTimestamp)
                .isBetween(startTimestamp, endTimestamp)
                .isLessThan(transformationEndTimestamp)
        assertThat(transformationEndTimestamp)
                .isBetween(startTimestamp, endTimestamp)
                .isGreaterThan(transformationStartTimestamp)

        exception.let {
            assertThat(it.javaClass).isEqualTo(expectException.javaClass)
            assertThat(it.message).isEqualTo(expectException.message)
            assertThat(it.localizedMessage).isEqualTo(expectException.localizedMessage)
            assertThat(it.cause).isEqualTo(expectException.cause)
        }
    }

    fun sendRequestMessage(transformerId: String, correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest),
                                   TransformationDescriptor(listOf(DataDescriptor("".toInMemoryData(), TEXT_PLAIN)),
                                                            MediaTypeConstants.APPLICATION_JSON,
                                                            MapParameters.empty())) { message ->
            message.jmsCorrelationID = correlationId
            message.setStringProperty(TransformerJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

            message.setObjectProperty("send_back_alf_node", listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                                   "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
            message.setObjectProperty("send_back_targetMediaType_mimeType", MediaTypeConstants.TEXT_PLAIN.mimeType)
            message.setObjectProperty("send_back_targetMediaType_charset", MediaTypeConstants.TEXT_PLAIN.charset.toString())
            message.setObjectProperty("send_back_parameters", MapParameters(mapOf("key" to "value")).getAll())
            message.setObjectProperty("send_back_timeout", 3000)

            message
        }
    }
}