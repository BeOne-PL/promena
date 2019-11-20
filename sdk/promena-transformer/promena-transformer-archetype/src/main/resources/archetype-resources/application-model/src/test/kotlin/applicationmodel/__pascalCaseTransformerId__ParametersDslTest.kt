package ${package}.applicationmodel

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class ${pascalCaseTransformerId}ParametersDslTest {

    companion object {
        private const val mandatory = "value"
        private const val optional = "optional value"
        private const val optionalLimitedValue = 1
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ default parameters`() {
        ${camelCaseTransformerId}Parameters(
            mandatory = mandatory
        ).let {
            it.getMandatory() shouldBe mandatory

            shouldThrow<NoSuchElementException> {
                it.getOptional()
            }
            it.getOptionalOrNull() shouldBe null
            it.getOptionalOrDefault(optional) shouldBe optional

            shouldThrow<NoSuchElementException> {
                it.getOptionalLimitedValue()
            }
            it.getOptionalLimitedValueOrNull() shouldBe null
            it.getOptionalLimitedValueOrDefault(optionalLimitedValue) shouldBe optionalLimitedValue
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ all parameters`() {
        ${camelCaseTransformerId}Parameters(
            mandatory = mandatory,
            optional = optional,
            optionalLimitedValue = optionalLimitedValue
        ).let {
            it.getMandatory() shouldBe mandatory
            it.getOptional() shouldBe optional
            it.getOptionalLimitedValue() shouldBe optionalLimitedValue
        }
    }
}