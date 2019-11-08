package ${package}

import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit.jupiter.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Support

@ExtendWith(DockerExtension::class)
class ${pascalCaseTransformerId}TransformerSupportTest {

    @Test
    fun isSupported() {
        val dataDescriptor = mockk<DataDescriptor>()
        val targetMediaType = mockk<MediaType>()
        val parameters = mockk<Parameters>()

        mockkStatic(${pascalCaseTransformerId}Support::class)
        every { ${pascalCaseTransformerId}Support.isSupported(dataDescriptor, targetMediaType, parameters) } just Runs

        ${pascalCaseTransformerId}Transformer(mockk(), mockk(), mockk(), mockk())
            .isSupported(dataDescriptor, targetMediaType, parameters)

        verify(exactly = 1) { ${pascalCaseTransformerId}Support.isSupported(dataDescriptor, targetMediaType, parameters) }
    }
}