package pl.beone.promena.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class TransformationDescriptorDslTest {

    @Test
    fun transformationDescriptor() {
        val transformation = mockk<Transformation>()
        val dataDescriptor = mockk<DataDescriptor>()

        transformationDescriptor(transformation, dataDescriptor).let {
            it.transformation shouldBe transformation
            it.dataDescriptor shouldBe dataDescriptor
        }
    }
}