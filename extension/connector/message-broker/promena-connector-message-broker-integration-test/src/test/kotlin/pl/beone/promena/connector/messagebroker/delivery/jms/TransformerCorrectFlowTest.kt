package pl.beone.promena.connector.messagebroker.delivery.jms

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
import pl.beone.promena.connector.messagebroker.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.messagebroker.integrationtest.test.TransformerResponseConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_JSON
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:module-connector-activemq-test.properties")
class TransformerCorrectFlowTest {

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
    fun `send data to transformation request queue _ should transform and send result to response queue`() {
        val transformerId = "test-transformer"
        val location = "file:/tmp"
        val correlationId = UUID.randomUUID().toString()
        val transformationDescriptor =
                TransformationDescriptor(listOf(DataDescriptor("test".toInMemoryData(), TEXT_PLAIN)), APPLICATION_JSON, MapParameters.empty())
        val transformedData = """" {"test":"test"} """.toInMemoryData()

        every {
            transformationUseCase.transform(transformerId,
                                            transformationDescriptor,
                                            MapCommunicationParameters(mapOf("location" to location)))
        } returns listOf(TransformedDataDescriptor(transformedData, MapMetadata.empty()))

        //
        val startTimestamp = getTimestamp()
        sendRequestMessage(transformerId, location, transformationDescriptor, correlationId)
        val (headers, transformedDataDescriptors) = try {
            transformerResponseConsumer.getMessage(3000)
        } catch (e: IllegalStateException) {
            throw AssertionError("Couldn't get message from response queue")
        }
        val endTimestamp = getTimestamp()

        //
        assertThat(headers)
                .containsEntry(CORRELATION_ID, correlationId)
                .containsEntry(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)
                .containsKey(PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP)
                .containsKey(PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP)

        val transformationStartTimestamp = headers[PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP] as Long
        val transformationEndTimestamp = headers[PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP] as Long
        assertThat(transformationStartTimestamp)
                .isBetween(startTimestamp, endTimestamp)
                .isLessThan(transformationEndTimestamp)
        assertThat(transformationEndTimestamp)
                .isBetween(startTimestamp, endTimestamp)
                .isGreaterThan(transformationStartTimestamp)

        assertThat(transformedDataDescriptors)
                .hasSize(1)
        transformedDataDescriptors[0].let {
            assertThat(it.data.getBytes()).isEqualTo(transformedData.getBytes())
            assertThat(it.metadata.getAll()).isEmpty()
        }
    }

    private fun sendRequestMessage(transformerId: String,
                                   location: String,
                                   transformationDescriptor: TransformationDescriptor,
                                   correlationId: String) {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest), listOf(transformationDescriptor)) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

                setStringProperty(PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION, location)
            }
        }
    }
}