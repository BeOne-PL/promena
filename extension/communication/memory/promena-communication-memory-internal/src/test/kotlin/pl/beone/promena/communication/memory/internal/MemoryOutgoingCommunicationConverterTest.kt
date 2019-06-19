package pl.beone.promena.communication.memory.internal

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class MemoryOutgoingCommunicationConverterTest {

    @Test
    fun convert() {
        val transformedDataDescriptor = TransformedDataDescriptor(mockk(), mockk())

        MemoryOutgoingCommunicationConverter().convert(transformedDataDescriptor, mockk()) shouldBe transformedDataDescriptor
    }
}