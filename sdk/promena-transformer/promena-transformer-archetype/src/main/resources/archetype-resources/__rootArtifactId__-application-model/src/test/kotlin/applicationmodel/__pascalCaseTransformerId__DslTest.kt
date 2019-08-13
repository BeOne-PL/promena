package ${package}.applicationmodel

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants

class ${pascalCaseTransformerId}DslTest {

    @Test
    fun ${camelCaseTransformerId}Parameters() {
        ${camelCaseTransformerId}Parameters(example = "test", example2 = "test2").let {
            it.get(${pascalCaseTransformerId}ParametersConstants.EXAMPLE) shouldBe "test"
            it.get(${pascalCaseTransformerId}ParametersConstants.EXAMPLE2) shouldBe "test2"
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ no optional example2 parameter _ should throw NoSuchElementException`() {
        shouldThrow<NoSuchElementException> {
            ${camelCaseTransformerId}Parameters(example = "test")
                .get(${pascalCaseTransformerId}ParametersConstants.EXAMPLE2)
        }
    }

    @Test
    fun ${camelCaseTransformerId}Transformation() {
        ${camelCaseTransformerId}Transformation(MediaTypeConstants.TEXT_PLAIN, ${camelCaseTransformerId}Parameters(example = "test")).let {
            it.transformerId shouldBe ${pascalCaseTransformerId}Constants.TRANSFORMER_ID
            it.targetMediaType shouldBe MediaTypeConstants.TEXT_PLAIN
        }
    }
}