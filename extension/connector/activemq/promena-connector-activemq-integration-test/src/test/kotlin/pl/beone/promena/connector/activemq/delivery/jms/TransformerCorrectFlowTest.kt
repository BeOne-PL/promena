package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeInRange
import io.kotlintest.matchers.numerics.shouldBeLessThan
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
import org.springframework.jms.support.JmsHeaders
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.activemq.integrationtest.test.QueueClearer
import pl.beone.promena.connector.activemq.integrationtest.test.TestTransformerMockContext
import pl.beone.promena.connector.activemq.integrationtest.test.TransformationResponseConsumer
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_JSON
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [IntegrationTestApplication::class])
@TestPropertySource("classpath:module-connector-activemq-test.properties")
class TransformerCorrectFlowTest {

    companion object {
        private val transformerIds = listOf(TestTransformerMockContext.TRANSFORMER_ID)
        private const val location = "file:/tmp"
        private val correlationId = UUID.randomUUID().toString()
        private val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata())
        private val transformation = singleTransformation(TestTransformerMockContext.TRANSFORMER_ID, APPLICATION_JSON, emptyParameters())
        private val transformationDescriptor = TransformationDescriptor.of(transformation, dataDescriptor)
        private val transformedData = """" {"test":"test"} """.toMemoryData()
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

    @Before
    fun setUp() {
        mockkObject(transformationUseCase)
        clearMocks(transformationUseCase)

        queueClearer.dequeueQueues()
    }

    @Test
    fun `send data to transformation request queue _ should transform and send result to response queue`() {
        every {
            transformationUseCase.transform(
                transformation,
                dataDescriptor,
                communicationParameters("memory") + ("location" to location)
            )
        } answers {
            Thread.sleep(300)
            singleTransformedDataDescriptor(transformedData, emptyMetadata())
        }

        val startTimestamp = getTimestamp()
        sendRequestMessage()
        val (headers, performedTransformationDescriptor) = try {
            transformationResponseConsumer.getMessage(3000)
        } catch (e: IllegalStateException) {
            throw AssertionError("Couldn't get message from response queue")
        }
        val endTimestamp = getTimestamp()

        headers.let {
            it shouldContainAll mapOf(
                JmsHeaders.CORRELATION_ID to correlationId,
                PromenaJmsHeaders.TRANSFORMATION_HASH_CODE to transformationHashFunctionDeterminer.determine(transformerIds)
            )
            it shouldContainKey PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP
            it shouldContainKey PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP
        }

        val transformationStartTimestamp = headers[PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP] as Long
        val transformationEndTimestamp = headers[PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP] as Long

        transformationStartTimestamp.let {
            it.shouldBeInRange(startTimestamp..endTimestamp)
            it shouldBeLessThan transformationEndTimestamp
        }

        transformationEndTimestamp.let {
            it.shouldBeInRange(startTimestamp..endTimestamp)
            it shouldBeGreaterThan transformationStartTimestamp
        }

        (transformationEndTimestamp - transformationStartTimestamp) shouldBeGreaterThanOrEqual 300

        performedTransformationDescriptor.transformation shouldBe
                transformation
        performedTransformationDescriptor.transformedDataDescriptor.descriptors.let {
            it shouldHaveSize 1
            it[0].let { transformedDataDescriptor ->
                transformedDataDescriptor.data.getBytes() shouldBe
                        transformedData.getBytes()
                transformedDataDescriptor.metadata.getAll() shouldBe
                        emptyMap()
            }
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest), transformationDescriptor) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeaders.TRANSFORMATION_HASH_CODE, transformationHashFunctionDeterminer.determine(transformerIds))

                setStringProperty(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID, "memory")
                setStringProperty(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_PREFIX + "location", location)
            }
        }
    }
}