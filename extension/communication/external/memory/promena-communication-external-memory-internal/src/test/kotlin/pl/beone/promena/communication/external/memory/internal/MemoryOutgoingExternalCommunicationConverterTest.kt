package pl.beone.promena.communication.external.memory.internal

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata

class MemoryOutgoingExternalCommunicationConverterTest {

    @Test
    fun convert() {
        val data = "test".toMemoryData()

        MemoryOutgoingExternalCommunicationConverter()
            .convert(singleTransformedDataDescriptor(data, emptyMetadata()), communicationParameters("memory")) shouldBe
                singleTransformedDataDescriptor(data, emptyMetadata())
    }

}