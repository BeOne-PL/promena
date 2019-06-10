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
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = [HttpConnectorModuleConfig::class])
class TransformerHandlerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @Test
    fun `transform _ with empty communication parameters`() {
        `when`(transformationUseCase.transform(eq("default"),
                                               eq("request body".toByteArray()),
                                               eq(MapCommunicationParameters.empty())))
                .thenReturn("processed request body".toByteArray())

        webTestClient.post().uri("/transform/default")
                .body(BodyInserters.fromObject("request body".toByteArray()))
                .exchange()
                .expectStatus().isOk
                .expectBody<ByteArray>().isEqualTo("processed request body".toByteArray())
    }

    @Test
    fun `transform _ with location in communication parameters`() {
        `when`(transformationUseCase.transform(eq("default"),
                                               eq("request body".toByteArray()),
                                               eq(MapCommunicationParameters(mapOf("location" to "file:/tmp")))))
                .thenReturn("processed request body".toByteArray())

        webTestClient.post().uri("/transform/default?location=file:/tmp")
                .body(BodyInserters.fromObject("request body".toByteArray()))
                .exchange()
                .expectStatus().isOk
                .expectBody<ByteArray>().isEqualTo("processed request body".toByteArray())
    }

    @Test
    fun `transform _ throw TransformerNotFoundException _ should return BadRequest`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerNotFoundException("exception message"))

        webTestClient.post().uri("/transform/default")
                .body(BodyInserters.fromObject("noMatter".toByteArray()))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("message").isEqualTo("exception message")
    }

    @Test
    fun `transform _ bad url _ should return NotFound`() {
        webTestClient.post().uri("/transform")
                .body(BodyInserters.fromObject("noMatter".toByteArray()))
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
    }

    @Test
    fun `transform _ throw TransformerTimeoutException _ should return RequestTimeout`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerTimeoutException("exception message"))

        webTestClient.post().uri("/transform/default")
                .body(BodyInserters.fromObject("noMatter".toByteArray()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.REQUEST_TIMEOUT)
                .expectBody()
                .jsonPath("message").isEqualTo("exception message")
    }

    @Test
    fun `transform _ throw TransformerException _ should return InternalServerError`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerException("exception message"))

        webTestClient.post().uri("/transform/default")
                .body(BodyInserters.fromObject("noMatter".toByteArray()))
                .exchange()
                .expectStatus().is5xxServerError
                .expectBody()
                .jsonPath("message").isEqualTo("exception message")
    }

    @Test
    fun `transform _ throw unhandled IllegalArgumentException _ should return InternalServerError`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(IllegalArgumentException("exception message"))

        webTestClient.post().uri("/transform/default")
                .body(BodyInserters.fromObject("noMatter".toByteArray()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("message").isEqualTo("exception message")
    }
}