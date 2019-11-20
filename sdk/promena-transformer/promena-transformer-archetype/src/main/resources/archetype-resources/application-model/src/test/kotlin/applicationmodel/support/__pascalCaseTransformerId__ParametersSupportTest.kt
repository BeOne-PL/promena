package ${package}.applicationmodel.support

import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import ${package}.applicationmodel.${pascalCaseTransformerId}Support.ParametersSupport.isSupported
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters

class ${pascalCaseTransformerId}ParametersSupportTest {

    @Test
    fun `isSupported _ default parameters`() {
        shouldNotThrow<TransformationNotSupportedException> {
            isSupported(${camelCaseTransformerId}Parameters(mandatory = "value"))
        }
    }

    @Test
    fun `isSupported _ all parameters`() {
        shouldNotThrow<TransformationNotSupportedException> {
            isSupported(${camelCaseTransformerId}Parameters(mandatory = "value", optional = "optional value", optionalLimitedValue = 1))
        }
    }

    @Test
    fun `isSupported _ invalid optionalLimitedValue value`() {
        shouldThrow<TransformationNotSupportedException> {
            isSupported(${camelCaseTransformerId}Parameters(mandatory = "value", optionalLimitedValue = -1))
        }
    }
}