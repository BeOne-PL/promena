package pl.beone.promena.communication.memory.internal

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryIncomingCommunicationConverterTest {


    @Test
    fun convert() {
        val dataDescriptor = DataDescriptor(mockk(), MediaTypeConstants.TEXT_PLAIN)
        val communicationParameters = MapCommunicationParameters.empty()

        MemoryIncomingCommunicationConverter()
                .convert(dataDescriptor, communicationParameters) shouldBe dataDescriptor
    }
}