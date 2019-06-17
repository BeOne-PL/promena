package pl.beone.promena.connector.http.delivery.http

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import pl.beone.promena.connector.http.configuration.HttpConnectorModuleConfig
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = [HttpConnectorModuleConfig::class])
class TransformerHandlerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var serializationService: SerializationService

    @MockBean
    private lateinit var descriptorSerializationService: DescriptorSerializationService

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    companion object {
        private const val transformerId = "default"
        private val requestBody = "request body".toByteArray()
        private val transformationDescriptor = TransformationDescriptor(emptyList(), MediaTypeConstants.TEXT_PLAIN, MapParameters.empty())
        private val transformedDataDescriptors = listOf(TransformedDataDescriptor(InMemoryData(ByteArray(0)), MapMetadata.empty()))
        private val responseBody = "response body".toByteArray()
    }

    @Test
    fun `transform _ with empty communication parameters`() {
        `when`(descriptorSerializationService.deserialize(requestBody))
                .thenReturn(transformationDescriptor)

        `when`(transformationUseCase.transform(eq(transformerId),
                                               eq(transformationDescriptor),
                                               eq(MapCommunicationParameters.empty())))
                .thenReturn(transformedDataDescriptors)

        `when`(descriptorSerializationService.serialize(transformedDataDescriptors))
                .thenReturn(responseBody)

        webTestClient.post().uri("/transform/$transformerId")
                .body(BodyInserters.fromObject(requestBody))
                .exchange()
                .expectStatus().isOk
                .expectBody<ByteArray>().isEqualTo(responseBody)
    }

    @Test
    fun `transform _ with location in communication parameters`() {
        `when`(descriptorSerializationService.deserialize(requestBody))
                .thenReturn(transformationDescriptor)

        `when`(transformationUseCase.transform(eq(transformerId),
                                               eq(transformationDescriptor),
                                               eq(MapCommunicationParameters(mapOf("location" to "file:/tmp")))))
                .thenReturn(transformedDataDescriptors)

        `when`(descriptorSerializationService.serialize(transformedDataDescriptors))
                .thenReturn(responseBody)

        webTestClient.post().uri("/transform/$transformerId?location=file:/tmp")
                .body(BodyInserters.fromObject(requestBody))
                .exchange()
                .expectStatus().isOk
                .expectBody<ByteArray>().isEqualTo(responseBody)
    }

    @Test
    fun `transform _ throw TransformerNotFoundException _ should return InternalServerError with serialized exception`() {
        val exceptionMessage = "exception".toByteArray()

        `when`(descriptorSerializationService.deserialize(requestBody))
                .thenReturn(transformationDescriptor)

        `when`(serializationService.serialize(any<TransformerNotFoundException>()))
                .thenReturn(exceptionMessage)

        `when`(transformationUseCase.transform(eq(transformerId),
                                               eq(transformationDescriptor),
                                               eq(MapCommunicationParameters.empty())))
                .thenThrow(TransformerNotFoundException("exception"))

        webTestClient.post().uri("/transform/$transformerId")
                .body(BodyInserters.fromObject(requestBody))
                .exchange()
                .expectHeader().valueEquals("serialization-class", "pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException")
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody<ByteArray>().isEqualTo(exceptionMessage)
    }

    @Test
    fun `transform _ bad url _ should return NotFound`() {
        webTestClient.post().uri("/transform")
                .body(BodyInserters.fromObject(requestBody))
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
    }
}