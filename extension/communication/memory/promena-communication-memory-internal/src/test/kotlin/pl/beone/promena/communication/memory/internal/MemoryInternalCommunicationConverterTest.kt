package pl.beone.promena.communication.memory.internal

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.net.URI

class MemoryInternalCommunicationConverterTest {

    companion object {
        private val metadata = MapMetadata.empty()
    }

    @Test
    fun convert() {
        val data = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
        }
        val transformedDataDescriptor = TransformedDataDescriptor(data, metadata)

        MemoryInternalCommunicationConverter()
                .convert(transformedDataDescriptor) shouldBe transformedDataDescriptor
    }

    @Test
    fun `convert _ data hasn't memory implementation _ should get data and load into memory`() {
        val bytes = "test".toByteArray()

        val data = mockk<Data> {
            every { getLocation() } returns URI("file:/tmp")
            every { getBytes() } returns bytes
        }
        val transformedDataDescriptor = TransformedDataDescriptor(data, metadata)

        MemoryInternalCommunicationConverter()
                .convert(transformedDataDescriptor).data.getBytes() shouldBe bytes
    }
}