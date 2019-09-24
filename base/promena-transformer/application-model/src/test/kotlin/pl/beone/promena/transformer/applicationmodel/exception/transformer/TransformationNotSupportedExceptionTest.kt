package pl.beone.promena.transformer.applicationmodel.exception.transformer

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN

class TransformationNotSupportedExceptionTest {

    @Test
    fun unsupportedMediaType() {
        TransformationNotSupportedException.unsupportedMediaType(TEXT_PLAIN, APPLICATION_PDF).message shouldBe
                "Transformation (text/plain, UTF-8) -> (application/pdf, UTF-8) isn't supported"
    }

    @Test
    fun mandatoryParameter() {
        TransformationNotSupportedException.mandatoryParameter("parameter").message shouldBe
                "Parameter <parameter> is mandatory"
    }

    @Test
    fun unsupportedParameterType() {
        TransformationNotSupportedException.unsupportedParameterType("parameter", String::class.java).message shouldBe
                "Parameter <parameter> isn't type of <java.lang.String>"
    }

    @Test
    fun custom() {
        TransformationNotSupportedException.custom("It isn't possible to transform using given arguments").message shouldBe
                "It isn't possible to transform using given arguments"
    }
}