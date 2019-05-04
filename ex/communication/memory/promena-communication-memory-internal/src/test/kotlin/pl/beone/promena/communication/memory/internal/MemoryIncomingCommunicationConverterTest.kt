package pl.beone.promena.communication.memory.internal

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryIncomingCommunicationConverterTest {

    companion object {
        private val converter = MemoryIncomingCommunicationConverter()
    }

    @Test
    fun convert() {
        val dataDescriptor = DataDescriptor(mock(), MediaTypeConstants.TEXT_PLAIN)
        assertThat(converter.convert(dataDescriptor, mock())).isEqualTo(dataDescriptor)
    }
}