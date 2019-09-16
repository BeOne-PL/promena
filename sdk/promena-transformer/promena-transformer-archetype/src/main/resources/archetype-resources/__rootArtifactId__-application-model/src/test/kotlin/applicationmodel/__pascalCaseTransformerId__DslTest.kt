package ${package}.applicationmodel

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants.TRANSFORMER_ID
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE
import ${package}.applicationmodel.${pascalCaseTransformerId}ParametersConstants.EXAMPLE2

class ${pascalCaseTransformerId}DslTest {

    @Test
    fun ${camelCaseTransformerId}Parameters() {
        ${camelCaseTransformerId}Parameters(example = "test", example2 = "test2").let {
            it.get(EXAMPLE, String::class.java) shouldBe "test"
            it.get(EXAMPLE2, String::class.java) shouldBe "test2"
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ no optional example2 parameter _ should throw NoSuchElementException`() {
        shouldThrow<NoSuchElementException> {
            ${camelCaseTransformerId}Parameters(example = "test")
                .get(EXAMPLE2)
        }
    }

    @Test
    fun ${camelCaseTransformerId}Transformation() {
        ${camelCaseTransformerId}Transformation(TEXT_PLAIN, ${camelCaseTransformerId}Parameters(example = "test")).let {
            it.transformerId shouldBe TRANSFORMER_ID
            it.targetMediaType shouldBe TEXT_PLAIN
            it.parameters.get(EXAMPLE, String::class.java) shouldBe "test"
        }
    }
}