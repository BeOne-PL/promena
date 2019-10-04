package ${package}.applicationmodel

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants.TRANSFORMER_ID

class ${pascalCaseTransformerId}DslTest {

    @Test
    fun ${camelCaseTransformerId}Transformation() {
        ${camelCaseTransformerId}Transformation(TEXT_PLAIN, ${camelCaseTransformerId}Parameters(example = "value")).let {
            it.transformerId shouldBe TRANSFORMER_ID
            it.targetMediaType shouldBe TEXT_PLAIN
            it.parameters.getAll().size shouldBe 1
        }
    }
}