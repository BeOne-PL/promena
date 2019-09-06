package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.clearMocks
import io.mockk.mockkObject
import org.apache.activemq.command.ActiveMQQueue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter
import pl.beone.promena.connector.activemq.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.activemq.integrationtest.test.QueueClearer
import pl.beone.promena.connector.activemq.integrationtest.test.TestTransformerMockContext
import pl.beone.promena.connector.activemq.integrationtest.test.TransformationResponseConsumer
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_JSON
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:extension-connector-activemq-test.properties")
class TransformationCommunicationParametersValidationExceptionFlowTestIT {

    companion object {
        private val transformerIds = listOf(TestTransformerMockContext.TRANSFORMER_ID)
        private val correlationId = UUID.randomUUID().toString()
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.connector.activemq.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var queueClearer: QueueClearer

    @Autowired
    private lateinit var transformerResponseConsumer: TransformationResponseConsumer

    @Autowired
    private lateinit var transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @BeforeEach
    fun setUp() {
        mockkObject(transformationUseCase)
        clearMocks(transformationUseCase)

        queueClearer.dequeueQueues()
    }

    @Test
    fun `send data to transformation request queue _ should handle exception to response error queue`() {
        val startTimestamp = getTimestamp()
        sendRequestMessage()
        val (headers, exception) = try {
            transformerResponseConsumer.getErrorMessage(3000)
        } catch (e: IllegalStateException) {
            throw AssertionError("Couldn't get message from response error queue")
        }
        val endTimestamp = getTimestamp()

        headers.let {
            it shouldContainAll mapOf(
                CORRELATION_ID to correlationId,
                KryoMessageConverter.PROPERTY_SERIALIZATION_CLASS to
                        "pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException",
                PromenaJmsHeaders.TRANSFORMATION_HASH_CODE to transformationHashFunctionDeterminer.determine(transformerIds)
            )
            it shouldContainKey PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
            it shouldContainKey PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
        }

        validateTimestamps(
            headers[PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP] as Long,
            headers[PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP] as Long,
            startTimestamp,
            endTimestamp
        )

        exception.let {
            it should beInstanceOf(CommunicationParametersValidationException::class)
            it.message shouldBe "Headers must contain <promena_communication_parameter_id>"
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(
            ActiveMQQueue(queueRequest),
            transformationDescriptor(
                singleTransformation(TestTransformerMockContext.TRANSFORMER_ID, APPLICATION_JSON, emptyParameters()),
                singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata())
            )
        ) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeaders.TRANSFORMATION_HASH_CODE, transformationHashFunctionDeterminer.determine(transformerIds))
            }
        }
    }
}