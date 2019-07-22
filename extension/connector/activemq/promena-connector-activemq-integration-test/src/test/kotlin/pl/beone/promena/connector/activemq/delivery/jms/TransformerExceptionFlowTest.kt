package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeInRange
import io.kotlintest.matchers.numerics.shouldBeLessThan
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
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.integrationtest.IntegrationTestApplication
import pl.beone.promena.connector.activemq.integrationtest.test.QueueClearer
import pl.beone.promena.connector.activemq.integrationtest.test.TestTransformerMockContext
import pl.beone.promena.connector.activemq.integrationtest.test.TransformationResponseConsumer
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
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
@TestPropertySource("classpath:module-connector-activemq-test.properties")
class TransformerExceptionFlowTest {

    companion object {
        private val transformerIds = listOf(TestTransformerMockContext.TRANSFORMER_ID)
        private val correlationId = UUID.randomUUID().toString()
        private val expectedException = TransformationException(singleTransformation("test", TEXT_PLAIN, emptyParameters()), "Time expired")
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
            throw expectedException
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
                                      PromenaJmsHeaders.TRANSFORMATION_ID to transformationHashFunctionDeterminer.determine(transformerIds),
                                      "send_back_nodeRefs" to listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                                     "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
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

        exception.let {
            it should beInstanceOf(expectedException::class)
            (it as TransformationException).transformation shouldBe expectedException.transformation
            it.message shouldBe expectedException.message
            it.localizedMessage shouldBe expectedException.localizedMessage
            it.cause shouldBe expectedException.cause
        }
    }

    private fun sendRequestMessage() {
        jmsTemplate.convertAndSend(ActiveMQQueue(queueRequest),
                                   TransformationDescriptor.of(singleTransformation(TestTransformerMockContext.TRANSFORMER_ID,
                                                                                    APPLICATION_JSON,
                                                                                    emptyParameters()),
                                                               singleDataDescriptor("".toMemoryData(), TEXT_PLAIN, emptyMetadata()))) { message ->
            message.apply {
                jmsCorrelationID = correlationId
                setStringProperty(PromenaJmsHeaders.TRANSFORMATION_ID, transformationHashFunctionDeterminer.determine(transformerIds))

                setStringProperty(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID, "memory")

                setObjectProperty("send_back_nodeRefs", listOf("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f",
                                                               "workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c"))
            }
        }
    }
}