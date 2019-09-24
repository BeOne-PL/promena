package ${package}.applicationmodel.support

import io.kotlintest.shouldNotThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import ${package}.applicationmodel.${pascalCaseTransformerId}Support.ParametersSupport.isSupported
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters

class ${pascalCaseTransformerId}ParametersSupportTest {

    @Test
    fun `isSupported _ default parameters`() {
        shouldNotThrow<TransformationNotSupportedException> {
            isSupported(testConverterParameters(example = "test"))
        }
    }

    @Test
    fun `isSupported _ all parameters`() {
        shouldNotThrow<TransformationNotSupportedException> {
            isSupported(testConverterParameters(example = "test", example2 = "test2"))
        }
    }
}