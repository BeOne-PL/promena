package ${package}.applicationmodel

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import ${package}.applicationmodel.${pascalCaseTransformerId}Constants.TRANSFORMER_ID

class ${pascalCaseTransformerId}DslTest {

    @Test
    fun ${camelCaseTransformerId}Transformation() {
        with(${camelCaseTransformerId}Transformation(TEXT_PLAIN, ${camelCaseTransformerId}Parameters(mandatory = "value"))) {
            transformerId shouldBe TRANSFORMER_ID
            targetMediaType shouldBe TEXT_PLAIN
            parameters.getAll().size shouldBe 1
        }
    }
}