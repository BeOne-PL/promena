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
        with(
            ${camelCaseTransformerId}Parameters(
                mandatory = mandatory
            )
        ) {
            getMandatory() shouldBe mandatory

            shouldThrow<NoSuchElementException> { getOptional() }
            getOptionalOrNull() shouldBe null
            getOptionalOrDefault(optional) shouldBe optional

            shouldThrow<NoSuchElementException> { getOptionalLimitedValue() }
            getOptionalLimitedValueOrNull() shouldBe null
            getOptionalLimitedValueOrDefault(optionalLimitedValue) shouldBe optionalLimitedValue
        }
    }

    @Test
    fun `${camelCaseTransformerId}Parameters _ all parameters`() {
        with(
            ${camelCaseTransformerId}Parameters(
                mandatory = mandatory,
                optional = optional,
                optionalLimitedValue = optionalLimitedValue
            )
        ) {
            getMandatory() shouldBe mandatory
            getOptional() shouldBe optional
            getOptionalLimitedValue() shouldBe optionalLimitedValue
        }
    }
}