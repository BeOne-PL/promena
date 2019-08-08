package pl.beone.promena.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class PerformedTransformationDescriptorDslTest {

    @Test
    fun performedTransformationDescriptor() {
        val transformation = mockk<Transformation>()
        val transformedDataDescriptor = mockk<TransformedDataDescriptor>()

        performedTransformationDescriptor(transformation, transformedDataDescriptor).let {
            it.transformation shouldBe transformation
            it.transformedDataDescriptor shouldBe transformedDataDescriptor
        }
    }
}