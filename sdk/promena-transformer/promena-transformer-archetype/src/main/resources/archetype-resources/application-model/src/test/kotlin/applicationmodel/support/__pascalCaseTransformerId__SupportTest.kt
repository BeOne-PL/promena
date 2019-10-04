package ${package}.applicationmodel.support

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Support.MediaTypeSupport
import ${package}.applicationmodel.${pascalCaseTransformerId}Support.ParametersSupport
import ${package}.applicationmodel.${pascalCaseTransformerId}Support.isSupported

class ${pascalCaseTransformerId}SupportTest {

    @BeforeEach
    fun setUp() {
        mockkObject(MediaTypeSupport)
        mockkObject(ParametersSupport)
    }

    @Test
    fun isSupported() {
        val mediaType = mockk<MediaType>()
        val dataDescriptor = dataDescriptor(singleDataDescriptor(mockk(), mediaType, mockk()))
        val targetMediaType = mockk<MediaType>()
        val parameters = mockk<Parameters>()

        every { MediaTypeSupport.isSupported(mediaType, targetMediaType) } just Runs
        every { ParametersSupport.isSupported(parameters) } just Runs

        isSupported(dataDescriptor, targetMediaType, parameters)

        verify(exactly = 1) { MediaTypeSupport.isSupported(mediaType, targetMediaType) }
        verify(exactly = 1) { ParametersSupport.isSupported(parameters) }
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(MediaTypeSupport)
        unmockkObject(ParametersSupport)
    }
}