package pl.beone.promena.connector.http.delivery.http

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import org.hamcrest.CoreMatchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException

@RunWith(SpringRunner::class)
@WebMvcTest(TransformerController::class)
@ContextConfiguration(classes = [TransformerController::class])
class TransformerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var transformationUseCase: TransformationUseCase

    @Test
    fun `transform _ with empty communication parameters`() {
        `when`(transformationUseCase.transform(eq("default"),
                eq("request body".toByteArray()),
                eq(MapCommunicationParameters.empty())))
                .thenReturn("processed request body".toByteArray())

        mockMvc.perform(post("/transform/default")
                .content(String("request body".toByteArray())))
                .andExpect(status().isOk)
                .andExpect(content().string(containsString("processed request body")))
    }

    @Test
    fun `transform _ with location in communication parameters`() {
        `when`(transformationUseCase.transform(eq("default"),
                eq("request body".toByteArray()),
                eq(MapCommunicationParameters(mapOf("location" to "file:/tmp")))))
                .thenReturn("processed request body".toByteArray())

        mockMvc.perform(post("/transform/default?location=file:/tmp")
                .content(String("request body".toByteArray())))
                .andExpect(status().isOk)
                .andExpect(content().string(containsString("processed request body")))
    }

    @Test
    fun `transform _ throw TransformerNotFoundException _ should return BadRequest`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerNotFoundException("exception message"))

        mockMvc.perform(post("/transform/default")
                .content("noMatter"))
                .andExpect(status().isBadRequest)
                .andExpect(status().reason("exception message"))
    }

    @Test
    fun `transform _ throw TransformerTimeoutException _ should return RequestTimeout`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerTimeoutException("exception message"))

        mockMvc.perform(post("/transform/default")
                .content("noMatter"))
                .andExpect(status().isRequestTimeout)
                .andExpect(status().reason("exception message"))
    }

    @Test
    fun `transform _ throw TransformerException _ should return InternalServerError`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(TransformerException("exception message"))

        mockMvc.perform(post("/transform/default")
                .content("noMatter"))
                .andExpect(status().isInternalServerError)
                .andExpect(status().reason("exception message"))
    }

    @Test
    fun `transform _ throw unhandled IllegalArgumentException _ should return InternalServerError`() {
        `when`(transformationUseCase.transform(any(), any<ByteArray>(), anyOrNull()))
                .thenThrow(IllegalArgumentException("exception message"))

        mockMvc.perform(post("/transform/default")
                .content("noMatter"))
                .andExpect(status().isInternalServerError)
                .andExpect(status().reason("exception message"))
    }
}