package ${package}.applicationmodel

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class ${pascalCaseTransformerId}ParametersDslTest {

    @Test
    fun `${camelCaseTransformerId}Parameters _ default parameters`() {
        val example = "value"

        ${camelCaseTransformerId}Parameters(example = example).let {
            it.getExample() shouldBe "value"
            shouldThrow<NoSuchElementException> {
                it.getExample2()
            }
            it.getExample2OrNull() shouldBe null
            it.getExample2OrDefault("default") shouldBe "default"
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ all parameters`() {
        val example = "value"
        val example2 = "value2"

        ${camelCaseTransformerId}Parameters(example = example, example2 = example2).let {
            it.getExample() shouldBe example
            it.getExample2() shouldBe example2
            it.getExample2OrNull() shouldBe example2
            it.getExample2OrDefault("default") shouldBe example2
        }
    }
}