package pl.beone.promena.communication.file.utils

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.toFileData
import java.io.File

class FileDescriptorConverterTest {

    @Test
    fun `convert _ dataDescriptor`() {
        val dataContent = "data".toByteArray()
        val data = mockk<Data> {
            every { getInputStream() } returns dataContent.inputStream()
            every { delete() } just Runs
        }
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val metadata = mockk<Metadata>()

        val location = createTempDir().toURI()

        val dataContent2 = "data2".toByteArray()
        val data2 = createFileData(dataContent2, File(location))
        val mediaType2 = MediaTypeConstants.APPLICATION_PDF
        val metadata2 = mockk<Metadata>()

        val dataContent3 = "data3".toByteArray()
        val data3 = createFileData(dataContent3)
        val mediaType3 = MediaTypeConstants.APPLICATION_PDF
        val metadata3 = mockk<Metadata>()

        FileDescriptorConverter(location).convert(
            singleDataDescriptor(data, mediaType, metadata) +
                    singleDataDescriptor(data2, mediaType2, metadata2) +
                    singleDataDescriptor(data3, mediaType3, metadata3)
        ).let {
            it.descriptors shouldHaveSize 3

            it.descriptors[0].let { dataDescriptor ->
                dataDescriptor.data shouldBe instanceOf(FileData::class)
                dataDescriptor.data shouldBe data2
                dataDescriptor.mediaType shouldBe mediaType2
                dataDescriptor.metadata shouldBe metadata2
            }
            data2.exists() shouldBe true

            it.descriptors[1].let { dataDescriptor ->
                dataDescriptor.data shouldBe instanceOf(FileData::class)
                dataDescriptor.data.getBytes() shouldBe dataContent
                dataDescriptor.mediaType shouldBe mediaType
                dataDescriptor.metadata shouldBe metadata
            }

            it.descriptors[2].let { dataDescriptor ->
                dataDescriptor.data shouldBe instanceOf(FileData::class)
                dataDescriptor.data.getBytes() shouldBe dataContent3
                dataDescriptor.mediaType shouldBe mediaType3
                dataDescriptor.metadata shouldBe metadata3
            }
            data3.exists() shouldBe false
        }
    }

    @Test
    fun `convert _ transformedDataDescriptor`() {
        val dataContent = "data".toByteArray()
        val data = mockk<Data> {
            every { getInputStream() } returns dataContent.inputStream()
            every { delete() } just Runs
        }
        val mediaType = MediaTypeConstants.TEXT_PLAIN
        val metadata = mockk<Metadata>()

        val location = createTempDir().toURI()

        val dataContent2 = "data2".toByteArray()
        val data2 = createFileData(dataContent2, File(location))
        val metadata2 = mockk<Metadata>()

        val dataContent3 = "data3".toByteArray()
        val data3 = createFileData(dataContent3)
        val metadata3 = mockk<Metadata>()

        FileDescriptorConverter(location).convert(
            singleTransformedDataDescriptor(data, metadata) +
                    singleTransformedDataDescriptor(data2, metadata2) +
                    singleTransformedDataDescriptor(data3, metadata3)
        ).let {
            it.descriptors shouldHaveSize 3

            it.descriptors[0].let { transformedDataDescriptor ->
                transformedDataDescriptor.data shouldBe instanceOf(FileData::class)
                transformedDataDescriptor.data shouldBe data2
                transformedDataDescriptor.metadata shouldBe metadata2
            }
            data2.exists() shouldBe true

            it.descriptors[1].let { transformedDataDescriptor ->
                transformedDataDescriptor.data shouldBe instanceOf(FileData::class)
                transformedDataDescriptor.data.getBytes() shouldBe dataContent
                transformedDataDescriptor.metadata shouldBe metadata
            }

            it.descriptors[2].let { transformedDataDescriptor ->
                transformedDataDescriptor.data shouldBe instanceOf(FileData::class)
                transformedDataDescriptor.data.getBytes() shouldBe dataContent3
                transformedDataDescriptor.metadata shouldBe metadata3
            }
            data3.exists() shouldBe false
        }
    }

    private fun createFileData(byteArray: ByteArray, directory: File? = null): FileData =
        createTempFile(directory = directory).apply {
            writeBytes(byteArray)
        }.toFileData()
}