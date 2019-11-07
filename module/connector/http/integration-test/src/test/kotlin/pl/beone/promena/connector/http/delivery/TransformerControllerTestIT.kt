package pl.beone.promena.connector.http.delivery

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.connector.http.applicationmodel.PromenaHttpHeaders.SERIALIZATION_CLASS
import pl.beone.promena.connector.http.configuration.HttpConnectorModuleConfig
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [HttpConnectorModuleConfig::class]
)
class TransformerControllerTestIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var serializationService: SerializationService

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    companion object {
        private const val transformEndpoint = "/transform"

        private val requestBody = "request body".toByteArray()
        private val transformation = singleTransformation("default", TEXT_PLAIN, emptyParameters())
        private val dataDescriptor = emptyDataDescriptor()
        private val communicationParameters = communicationParameters("")
        private val transformationDescriptor = transformationDescriptor(transformation, dataDescriptor, communicationParameters)
        private val transformedDataDescriptor = singleTransformedDataDescriptor("".toMemoryData(), emptyMetadata())
        private val performedTransformationDescriptor = performedTransformationDescriptor(transformedDataDescriptor)
        private val responseBody = "response body".toByteArray()
    }

    @BeforeEach
    fun setUp() {
        mockkObject(serializationService)
        clearMocks(serializationService)

        mockkObject(transformationUseCase)
        clearMocks(transformationUseCase)

        mockkObject(serializationService)
        clearMocks(serializationService)
    }

    @Test
    fun transform() {
        every { serializationService.deserialize(requestBody, getClazz<TransformationDescriptor>()) } returns transformationDescriptor
        every { serializationService.serialize(performedTransformationDescriptor) } returns responseBody

        every { transformationUseCase.transform(transformation, dataDescriptor, communicationParameters) } returns transformedDataDescriptor

        webTestClient.post().uri(transformEndpoint)
            .body(BodyInserters.fromObject(requestBody))
            .exchange()
            .expectStatus().isOk
            .expectBody<ByteArray>().isEqualTo(responseBody)
    }

    @Test
    fun `transform _ should throw TransformationException and return InternalServerError with serialized exception`() {
        val exception = TransformationException("exception", RuntimeException::class.java)
        val messageByteArray = exception.message!!.toByteArray()

        every { serializationService.deserialize(requestBody, getClazz<TransformationDescriptor>()) } returns transformationDescriptor
        every { serializationService.serialize(any<TransformationException>()) } returns messageByteArray

        every { transformationUseCase.transform(transformation, dataDescriptor, communicationParameters) } throws exception

        webTestClient.post().uri(transformEndpoint)
            .body(BodyInserters.fromObject(requestBody))
            .exchange()
            .expectHeader()
            .valueEquals(SERIALIZATION_CLASS, "pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException")
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody<ByteArray>().isEqualTo(messageByteArray)
    }

    @Test
    fun `transform _ bad url _ should return NotFound`() {
        webTestClient.post().uri("/absent")
            .body(BodyInserters.fromObject(requestBody))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
    }
}