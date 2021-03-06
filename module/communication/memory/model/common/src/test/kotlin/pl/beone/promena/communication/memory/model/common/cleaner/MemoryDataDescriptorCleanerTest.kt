package pl.beone.promena.communication.memory.model.common.cleaner

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.data.Data

class MemoryDataDescriptorCleanerTest {

    @Test
    fun clean() {
        val commonData = mockk<Data>()
        val data = mockk<Data>()
        val transformedData = mockk<Data>()

        MemoryDataDescriptorCleaner.clean(
            singleDataDescriptor(commonData, APPLICATION_PDF, mockk()) +
                    singleDataDescriptor(data, APPLICATION_PDF, mockk()),
            singleTransformedDataDescriptor(transformedData, mockk()) +
                    singleTransformedDataDescriptor(commonData, mockk())
        )

        verify(exactly = 0) { commonData.delete() }
        verify(exactly = 0) { data.delete() }
        verify(exactly = 0) { transformedData.delete() }
    }
}