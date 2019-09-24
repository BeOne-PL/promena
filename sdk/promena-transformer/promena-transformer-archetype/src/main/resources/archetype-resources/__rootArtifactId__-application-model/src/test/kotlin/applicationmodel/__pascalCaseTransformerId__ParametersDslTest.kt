package ${package}.applicationmodel

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class ${pascalCaseTransformerId}ParametersDslTest {

    @Test
    fun `${camelCaseTransformerId}Parameters _ default parameters`() {
        ${camelCaseTransformerId}Parameters(example = "test").let {
            it.getExample() shouldBe "test"
            shouldThrow<NoSuchElementException> {
                it.getExample2()
            }
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ all parameters`() {
        ${camelCaseTransformerId}Parameters(example = "test", example2 = "test2").let {
            it.getExample() shouldBe "test"
            it.getExample2() shouldBe "test2"
        }
    }
}