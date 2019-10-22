package pl.beone.promena.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class PerformedTransformationDescriptorDslTest {

    @Test
    fun performedTransformationDescriptor() {
        val transformedDataDescriptor = mockk<TransformedDataDescriptor>()

        performedTransformationDescriptor(transformedDataDescriptor).let {
            it.transformedDataDescriptor shouldBe transformedDataDescriptor
        }
    }
}