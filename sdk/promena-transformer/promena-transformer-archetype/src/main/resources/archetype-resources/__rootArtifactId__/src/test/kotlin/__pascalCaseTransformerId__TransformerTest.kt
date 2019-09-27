package ${package}

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.beone.lib.junit5.extension.docker.external.DockerExtension
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
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
                val descriptors = transformedDataDescriptor.descriptors
                withClue("Transformed data should contain only <1> element") { descriptors shouldHaveSize 1 }

                descriptors[0].let {
                    it.data.getBytes() shouldBe dataContent.toByteArray()
                    it.metadata shouldBe metadata
                }
            }
    }
}