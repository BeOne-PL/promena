package ${package}

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit.jupiter.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters
import ${package}.util.create${pascalCaseTransformerId}Transformer
import ${package}.util.getResourceAsBytes
import ${package}.model.Resource.Text.EXAMPLE

@ExtendWith(DockerExtension::class)
class ${pascalCaseTransformerId}TransformerTest {

    @Test
    fun transform() {
        val mediaType = TEXT_PLAIN
        val metadata = emptyMetadata()

        create${pascalCaseTransformerId}Transformer()
            .transform(
                singleDataDescriptor(getResourceAsBytes(EXAMPLE).toMemoryData(), mediaType, metadata),
                TEXT_PLAIN,
                ${camelCaseTransformerId}Parameters(mandatory = "value")
            ).let { transformedDataDescriptor ->
                val descriptors = transformedDataDescriptor.descriptors
                withClue("Transformed data should contain only <1> element") { descriptors shouldHaveSize 1 }

                descriptors[0].let {
                    it.data.getBytes() shouldBe "example content".toByteArray()
                    it.metadata shouldBe metadata
                }
            }
    }
}