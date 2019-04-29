package pl.beone.promena.communication.memory.internal

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class MemoryOutgoingCommunicationConverterTest {

    companion object {
        private val converter = MemoryOutgoingCommunicationConverter()
    }

    @Test
    fun convert() {
        val transformedDataDescriptor = TransformedDataDescriptor(mock(), mock())
        assertThat(converter.convert(transformedDataDescriptor, mock())).isEqualTo(transformedDataDescriptor)
    }
}