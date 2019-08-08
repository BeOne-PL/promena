package ${package}

import ${package}.applicationmodel.${pascalCaseTransformerId}Constants
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit5.extension.docker.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerCouldNotTransformException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters

@ExtendWith(DockerExtension::class)
class ${pascalCaseTransformerId}TransformerTest {

    @Test
    fun transform() {
        val dataContent = "test"
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val metadata = emptyMetadata()

        AppenderKotlinTransformer(mockk())
            .transform(
                singleDataDescriptor(dataContent.toMemoryData(), mediaType, metadata),
                MediaTypeConstants.TEXT_PLAIN,
                appenderKotlinParameters(example = "test")
            ).let {
                val descriptors = it.descriptors
                descriptors shouldHaveSize 1

                descriptors[0].let { singleDataDescriptor ->
                    singleDataDescriptor.data.getBytes() shouldBe dataContent.toByteArray()
                    singleDataDescriptor.metadata shouldBe metadata
                }
            }
    }

    @Test
    fun canTransform_targetMediaTypeIsNotTextPlain_shouldThrowTransformerCouldNotTransformException() {
        shouldThrow<TransformerCouldNotTransformException> {
            AppenderKotlinTransformer(mockk())
                .canTransform(
                    emptyDataDescriptor(),
                    MediaTypeConstants.APPLICATION_PDF,
                    appenderKotlinParameters(example = "test")
                )
        }.message shouldBe "Supported transformation: text/plain -> text/plain"
    }

    @Test
    fun canTransform_dataDescriptorMediaTypeIsNotTextPlain_shouldThrowTransformerCouldNotTransformException() {
        shouldThrow<TransformerCouldNotTransformException> {
            AppenderKotlinTransformer(mockk())
                .canTransform(
                    singleDataDescriptor("".toMemoryData(), MediaTypeConstants.APPLICATION_PDF, emptyMetadata()),
                    MediaTypeConstants.TEXT_PLAIN,
                    appenderKotlinParameters(example = "test")
                )
        }.message shouldBe "Supported transformation: text/plain -> text/plain"
    }

    @Test
    fun canTransform_noMandatoryParameter_shouldThrowTransformerCouldNotTransformException() {
        shouldThrow<TransformerCouldNotTransformException> {
            AppenderKotlinTransformer(mockk())
                .canTransform(
                    emptyDataDescriptor(),
                    MediaTypeConstants.TEXT_PLAIN,
                    emptyParameters()
                )
        }.message shouldBe "Mandatory parameter: ${AppenderKotlinConstants.Parameters.EXAMPLE}"
    }
}