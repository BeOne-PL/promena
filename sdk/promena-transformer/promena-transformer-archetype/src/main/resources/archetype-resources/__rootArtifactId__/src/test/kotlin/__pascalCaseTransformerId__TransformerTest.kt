package ${package}

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.lib.dockertestrunner.external.DockerTestRunner
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerCouldNotTransformException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters

@RunWith(DockerTestRunner::class)
class ${pascalCaseTransformerId}TransformerTest {

    companion object {
        private val communicationParameters = communicationParameters("memory")
    }

    @Test
    fun transform() {
        val dataContent = "test"
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val metadata = emptyMetadata()

        ${pascalCaseTransformerId}Transformer(communicationParameters)
            .transform(
                singleDataDescriptor(dataContent.toMemoryData(), mediaType, metadata),
                MediaTypeConstants.TEXT_PLAIN,
                ${camelCaseTransformerId}Parameters(example = "test")
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
            ${pascalCaseTransformerId}Transformer(communicationParameters)
                .canTransform(
                    emptyDataDescriptor(),
                    MediaTypeConstants.APPLICATION_PDF,
                    ${camelCaseTransformerId}Parameters(example = "test")
                )
        }.message shouldBe "Supported transformations: text/plain -> text/plain"
    }

    @Test
    fun canTransform_dataDescriptorMediaTypeIsNotTextPlain_shouldThrowTransformerCouldNotTransformException() {
        shouldThrow<TransformerCouldNotTransformException> {
            ${pascalCaseTransformerId}Transformer(communicationParameters)
                .canTransform(
                    singleDataDescriptor("".toMemoryData(), MediaTypeConstants.APPLICATION_PDF, emptyMetadata()),
                    MediaTypeConstants.TEXT_PLAIN,
                    ${camelCaseTransformerId}Parameters(example = "test")
                )
        }.message shouldBe "Supported transformations: text/plain -> text/plain"
    }

    @Test
    fun canTransform_noMandatoryParameter_shouldThrowTransformerCouldNotTransformException() {
        shouldThrow<TransformerCouldNotTransformException> {
            ${pascalCaseTransformerId}Transformer(communicationParameters)
                .canTransform(
                    emptyDataDescriptor(),
                    MediaTypeConstants.TEXT_PLAIN,
                    emptyParameters()
                )
        }.message shouldBe "Mandatory parameters: ${${pascalCaseTransformerId}Constants.Parameters.EXAMPLE}"
    }
}