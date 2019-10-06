package pl.beone.promena.connector.normal.http.delivery

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
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.REQUEST_TIMEOUT
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.configuration.NormalHttpConnectorModuleConfig
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import java.util.concurrent.TimeoutException
import kotlin.text.Charsets.ISO_8859_1

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [NormalHttpConnectorModuleConfig::class]
)
class TransformationNormalControllerTestIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    companion object {
        private val transformation =
            singleTransformation("transformerName", "transformerSubName", mediaType(TEXT_PLAIN.mimeType, ISO_8859_1), emptyParameters())
        private val dataDescriptor = singleDataDescriptor("1".toMemoryData(), TEXT_PLAIN, emptyMetadata())
        private val dataDescriptor2 = singleDataDescriptor("2".toMemoryData(), TEXT_PLAIN, emptyMetadata())
        private val dataDescriptor3 =
            singleDataDescriptor("3".toMemoryData(), mediaType(TEXT_PLAIN.mimeType, ISO_8859_1.name()), emptyMetadata())
        private val dataDescriptor4 = singleDataDescriptor("4".toMemoryData(), mediaType(TEXT_PLAIN.mimeType, ISO_8859_1.name()), emptyMetadata())
        private val communicationParameters = memoryCommunicationParameters()
        private val transformedDataDescriptor = singleTransformedDataDescriptor("response body".toMemoryData(), emptyMetadata())
    }

    @BeforeEach
    fun setUp() {
        mockkObject(transformationUseCase)
        clearMocks(transformationUseCase)
    }

    @Test
    fun `transform _ single transformation with all parameters and all data media type formats`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor + dataDescriptor2 + dataDescriptor3 + dataDescriptor4, communicationParameters)
        } returns transformedDataDescriptor

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
            part("2", dataDescriptor2.data.getBytes(), parseMediaType(dataDescriptor2.mediaType.mimeType))
            part(
                "3",
                dataDescriptor3.data.getBytes(),
                parseMediaType(dataDescriptor3.mediaType.mimeType + "; charset=" + dataDescriptor3.mediaType.charset)
            )
            part("4", dataDescriptor4.data.getBytes())
                .header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor4.mediaType.mimeType)
                .header(DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET, dataDescriptor4.mediaType.charset.name())
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(transformation.targetMediaType.mimeType + "; charset=" + transformation.targetMediaType.charset.name())
            .expectBody<ByteArray>().isEqualTo(transformedDataDescriptor.data.getBytes())
    }

    @Test
    fun `transform _ many transformation with default parameters`() {
        val transformation2 = singleTransformation("converter", APPLICATION_PDF, emptyParameters())

        every {
            transformationUseCase.transform(transformation next transformation2, dataDescriptor, communicationParameters)
        } returns transformedDataDescriptor

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .header("transformation2-transformerId-name", transformation2.transformerId.name)
            .header("transformation2-mediaType-mimeType", transformation2.targetMediaType.mimeType)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(transformation2.targetMediaType.mimeType + "; charset=" + transformation2.targetMediaType.charset.name())
            .expectBody<ByteArray>().isEqualTo(transformedDataDescriptor.data.getBytes())
    }

    @Test
    fun `transform _ no transformation-transformerId-name header _ should return BadRequest`() {
        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)

            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("There is no header <transformation-transformerId-name>")
    }

    @Test
    fun `transform _ no dataDescriptor-mediaType-mimeType or Content-Type header _ should return BadRequest`() {
        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes())
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Part <1> headers don't contain <dataDescriptor-mediaType-mimeType> or <Content-Type> header")
    }

    @Test
    fun `transform _ more than one transformed data _ should return BadRequest`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } returns transformedDataDescriptor + transformedDataDescriptor

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("There is more than one transformed data: <2>")
    }

    @Test
    fun `transform _ TransformationException with causeClass TransformationNotSupportedException _ should return BadRequest`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws TransformationException(transformation, "Exception", TransformationNotSupportedException::class.java)

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }

    @Test
    fun `transform _ TransformationException with causeClass TransformerNotFoundException _ should return BadRequest`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws TransformationException(transformation, "Exception", TransformerNotFoundException::class.java)

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }

    @Test
    fun `transform _ TransformationException with causeClass TransformerTimeoutException _ should return RequestTimeout`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws TransformationException(transformation, "Exception", TransformerTimeoutException::class.java)

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isEqualTo(REQUEST_TIMEOUT)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }

    @Test
    fun `transform _ TransformationException with causeClass subclass of TimeoutException _ should return RequestException`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws TransformationException(transformation, "Exception", (object : TimeoutException() {})::class.java)

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isEqualTo(REQUEST_TIMEOUT)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }

    @Test
    fun `transform _ not handled exception _ should return InternalServerException`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws RuntimeException("Exception")

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }

    @Test
    fun `transform _ TransformationException with not known exception _ should return InternalServerException`() {
        every {
            transformationUseCase.transform(transformation, dataDescriptor, communicationParameters)
        } throws TransformationException(transformation, "Exception", RuntimeException::class.java)

        val body = MultipartBodyBuilder().apply {
            part("1", dataDescriptor.data.getBytes()).header(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE, dataDescriptor.mediaType.mimeType)
        }.build()

        webTestClient.post().uri("/normal/transform")
            .body(BodyInserters.fromMultipartData(body))
            .header("transformation-transformerId-name", transformation.transformerId.name)
            .header("transformation-transformerId-subName", transformation.transformerId.subName)
            .header("transformation-mediaType-mimeType", transformation.targetMediaType.mimeType)
            .header("transformation-mediaType-charset", transformation.targetMediaType.charset.name())
            .exchange()
            .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Exception")
    }
}