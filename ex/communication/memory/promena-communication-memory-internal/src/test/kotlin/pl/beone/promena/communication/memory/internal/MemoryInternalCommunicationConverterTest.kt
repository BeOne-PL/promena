package pl.beone.promena.communication.memory.internal

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class MemoryInternalCommunicationConverterTest {

    companion object {
        private val converter = MemoryInternalCommunicationConverter()
    }

    @Test
    fun convert() {
        val data = mock<Data> {
            on { getLocation() } doThrow UnsupportedOperationException()
        }
        val transformedDataDescriptor = TransformedDataDescriptor(data, mock())

        assertThat(converter.convert(transformedDataDescriptor)).isEqualTo(transformedDataDescriptor)
    }

    @Test
    fun `convert _ data hasn't memory implementation _ should get data and load into memory`() {
        val data = mock<Data> {
            on { getLocation() } doReturn URI("file:/tmp")
            on { getBytes() } doReturn "test".toByteArray()
        }
        val transformedDataDescriptor = TransformedDataDescriptor(data, mock())

        assertThat(converter.convert(transformedDataDescriptor).data.getBytes()).isEqualTo("test".toByteArray())
    }
}