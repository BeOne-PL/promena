package pl.beone.promena.communication.file.common.cleaner

import io.kotlintest.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.common.extension.exists
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.fileData

class FileDataDescriptorCleanerTest {

    @Test
    fun delete() {
        val mediaType = MediaTypeConstants.APPLICATION_PDF

        val commonData = mockk<Data> { every { delete() } just Runs }
        val data = mockk<Data> { every { delete() } just Runs }
        val transformedData = mockk<Data>()

        val commonFileData = fileData(createTempFile())
        val transformedCommonFileData = fileData(createTempFile())

        val file = createTempFile()
        val commonTheSameFileData = fileData(file)
        val transformedCommonTheSameFileData = fileData(file)

        FileDataDescriptorCleaner.clean(
            singleDataDescriptor(commonData, mediaType, mockk()) +
                    singleDataDescriptor(data, mediaType, mockk()) +
                    singleDataDescriptor(commonFileData, mediaType, mockk()) +
                    singleDataDescriptor(commonTheSameFileData, mediaType, mockk()),
            singleTransformedDataDescriptor(commonData, mockk()) +
                    singleTransformedDataDescriptor(transformedData, mockk()) +
                    singleTransformedDataDescriptor(transformedCommonFileData, mockk()) +
                    singleTransformedDataDescriptor(transformedCommonTheSameFileData, mockk())
        )

        verify(exactly = 1) { commonData.delete() }
        verify(exactly = 1) { data.delete() }
        verify(exactly = 0) { transformedData.delete() }

        commonFileData.exists() shouldBe false
        transformedCommonFileData.exists() shouldBe true

        commonTheSameFileData.exists() shouldBe true
        transformedCommonTheSameFileData.exists() shouldBe true
    }
}
