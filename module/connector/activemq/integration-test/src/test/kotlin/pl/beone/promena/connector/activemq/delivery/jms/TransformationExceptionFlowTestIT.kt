package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.clearMocks
import io.mockk.every
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
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter.Companion.PROPERTY_SERIALIZATION_CLASS
import pl.beone.promena.connector.activemq.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.activemq.integrationtest.test.QueueClearer
import pl.beone.promena.connector.activemq.integrationtest.test.TestTransformerMockContext
import pl.beone.promena.connector.activemq.integrationtest.test.TransformationResponseConsumer
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_JSON
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.util.*
import java.util.concurrent.TimeoutException

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:extension-connector-activemq-test.properties")
class TransformationExceptionFlowTestIT {

    companion object {
        private val transformerIds = listOf(TestTransformerMockContext.TRANSFORMER_ID)
        private val correlationId = UUID.randomUUID().toString()
        private val expectedException = TransformationException("Time expired", TimeoutException::class.java)
    }

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Value("\${promena.connector.activemq.consumer.queue.request}")
    private lateinit var queueRequest: String

    @Autowired
    private lateinit var queueClearer: QueueClearer

    @Autowired
    private lateinit var transformationResponseConsumer: TransformationResponseConsumer

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
        every { transformationUseCase.transform(any(), any(), any()) } answers {
            Thread.sleep(300)
            throw expectedException
        }

        val startTimestamp = getTimestamp()
        sendRequestMessage()
        val (headers, exception) = try {
            transformationResponseConsumer.getErrorMessage(3000)
        } catch (e: IllegalStateException) {
            throw AssertionError("Couldn't get message from response error queue")
        }
        val endTimestamp = getTimestamp()

        headers.let {
            it shouldContainAll mapOf(
                CORRELATION_ID to correlationId,
                PROPERTY_SERIALIZATION_CLASS to "pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException",
                TRANSFORMATION_HASH_CODE to transformationHashFunctionDeterminer.determine(transformerIds),
                "send_back_nodeRefs" to listOf(
                    "workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                    "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"
                )
            )
            it shouldContainKey TRANSFORMATION_START_TIMESTAMP
            it shouldContainKey TRANSFORMATION_END_TIMESTAMP
        }

        val transformationStartTimestamp = headers[TRANSFORMATION_START_TIMESTAMP] as Long
        val transformationEndTimestamp = headers[TRANSFORMATION_END_TIMESTAMP] as Long
        validateTimestamps(transformationStartTimestamp, transformationEndTimestamp, startTimestamp, endTimestamp)
        (transformationEndTimestamp - transformationStartTimestamp) shouldBeGreaterThanOrEqual 300

        exception.let {
            it should beInstanceOf(expectedException::class)
            it.message shouldBe expectedException.message
            it.localizedMessage shouldBe expectedException.localizedMessage
            it.cause shouldBe expectedException.cause
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(
            ActiveMQQueue(queueRequest),
            transformationDescriptor(
                singleTransformation(TestTransformerMockContext.TRANSFORMER_ID, APPLICATION_JSON, emptyParameters()),
                singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata()),
                communicationParameters("")
            )
        ) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(TRANSFORMATION_HASH_CODE, transformationHashFunctionDeterminer.determine(transformerIds))

                setObjectProperty(
                    "send_back_nodeRefs",
                    listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f", "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c")
                )
            }
        }
    }
}