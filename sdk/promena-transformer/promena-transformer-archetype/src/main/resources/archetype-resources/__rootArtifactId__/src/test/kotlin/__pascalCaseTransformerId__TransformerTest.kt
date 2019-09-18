package ${package}

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit5.extension.docker.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters

@ExtendWith(DockerExtension::class)
class ${pascalCaseTransformerId}TransformerTest {

    @Test
    fun transform() {
        val dataContent = "test"
        val mediaType = TEXT_PLAIN
        val metadata = emptyMetadata()

        ${pascalCaseTransformerId}Transformer(mockk())
            .transform(
                singleDataDescriptor(dataContent.toMemoryData(), mediaType, metadata),
                TEXT_PLAIN,
                ${camelCaseTransformerId}Parameters(example = "test")
            ).let { transformedDataDescriptor ->
                withClue("Transformed data should contain only <1> element") { transformedDataDescriptor.descriptors shouldHaveSize 1 }

                transformedDataDescriptor.descriptors[0].let {
                    it.data.getBytes() shouldBe dataContent.toByteArray()
                    it.metadata shouldBe metadata
                }
            }
    }

    @Test
    fun isSupported_targetMediaTypeIsNotTextPlain_shouldThrowTransformationNotSupportedException() {
        shouldThrow<TransformationNotSupportedException> {
            ${pascalCaseTransformerId}Transformer(mockk())
                .isSupported(
                    emptyDataDescriptor(),
                    APPLICATION_PDF,
                    ${camelCaseTransformerId}Parameters(example = "test")
                )
        }.message shouldBe "Supported transformation: text/plain -> text/plain"
    }

    @Test
    fun isSupported_dataDescriptorMediaTypeIsNotTextPlain_shouldThrowTransformationNotSupportedException() {
        shouldThrow<TransformationNotSupportedException> {
            ${pascalCaseTransformerId}Transformer(mockk())
                .isSupported(
                    singleDataDescriptor("".toMemoryData(), APPLICATION_PDF, emptyMetadata()),
                    TEXT_PLAIN,
                    ${camelCaseTransformerId}Parameters(example = "test")
                )
        }.message shouldBe "Supported transformation: text/plain -> text/plain"
    }

    @Test
    fun isSupported_noMandatoryParameter_shouldThrowTransformationNotSupportedException() {
        shouldThrow<TransformationNotSupportedException> {
            ${pascalCaseTransformerId}Transformer(mockk())
                .isSupported(
                    emptyDataDescriptor(),
                    TEXT_PLAIN,
                    emptyParameters()
                )
        }.message shouldBe "Mandatory parameter: $EXAMPLE"
    }
}