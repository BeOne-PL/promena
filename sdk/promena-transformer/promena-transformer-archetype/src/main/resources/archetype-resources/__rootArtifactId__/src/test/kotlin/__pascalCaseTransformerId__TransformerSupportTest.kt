package ${package}

import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit5.extension.docker.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Support

@ExtendWith(DockerExtension::class)
class ${pascalCaseTransformerId}SupportTest {

    @BeforeEach
    fun setUp() {
        mockkObject(${pascalCaseTransformerId}Support)
    }

    @Test
    fun isSupported() {
        val dataDescriptor = mockk<DataDescriptor>()
        val targetMediaType = mockk<MediaType>()
        val parameters = mockk<Parameters>()

        every { ${pascalCaseTransformerId}Support.isSupported(dataDescriptor, targetMediaType, parameters) } just Runs

        ${pascalCaseTransformerId}Transformer(mockk())
            .isSupported(dataDescriptor, targetMediaType, parameters)

        verify(exactly = 1) { ${pascalCaseTransformerId}Support.isSupported(dataDescriptor, targetMediaType, parameters) }
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(${pascalCaseTransformerId}Support)
    }
}