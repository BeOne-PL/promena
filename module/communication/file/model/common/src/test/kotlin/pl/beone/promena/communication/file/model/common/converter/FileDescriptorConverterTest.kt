package pl.beone.promena.communication.file.model.common.converter

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.communication.file.model.common.extension.exists
import pl.beone.promena.communication.file.model.common.extension.toFile
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData
import pl.beone.promena.transformer.internal.model.data.file.toFileData
import java.io.File

class FileDescriptorConverterTest {

    @Test
    fun `convert _ dataDescriptor`() {
        val dataContent = "data".toByteArray()
        val data = mockk<Data> {
            every { getInputStream() } returns dataContent.inputStream()
            every { delete() } just Runs
        }
        val mediaType = TEXT_PLAIN
        val metadata = mockk<Metadata>()

        val directory = createTempDir().toURI().toFile()

        val dataContent2 = "data2".toByteArray()
        val data2 = createFileData(dataContent2, directory)
        val mediaType2 = APPLICATION_PDF
        val metadata2 = mockk<Metadata>()

        val dataContent3 = "data3".toByteArray()
        val data3 = createFileData(dataContent3)
        val mediaType3 = APPLICATION_PDF
        val metadata3 = mockk<Metadata>()

        with(
            FileDescriptorConverter(directory).convert(
                singleDataDescriptor(data, mediaType, metadata) +
                        singleDataDescriptor(data2, mediaType2, metadata2) +
                        singleDataDescriptor(data3, mediaType3, metadata3)
            )
        ) {
            descriptors shouldHaveSize 3

            with(descriptors[0]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data shouldBe data2
                this.mediaType shouldBe mediaType2
                this.metadata shouldBe metadata2
            }
            data2.exists() shouldBe true

            with(descriptors[1]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data.getBytes() shouldBe dataContent
                this.mediaType shouldBe mediaType
                this.metadata shouldBe metadata
            }

            with(descriptors[2]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data.getBytes() shouldBe dataContent3
                this.mediaType shouldBe mediaType3
                this.metadata shouldBe metadata3
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
        val metadata = mockk<Metadata>()

        val directory = createTempDir().toURI().toFile()

        val dataContent2 = "data2".toByteArray()
        val data2 = createFileData(dataContent2, directory)
        val metadata2 = mockk<Metadata>()

        val dataContent3 = "data3".toByteArray()
        val data3 = createFileData(dataContent3)
        val metadata3 = mockk<Metadata>()

        with(
            FileDescriptorConverter(directory).convert(
                singleTransformedDataDescriptor(data, metadata) +
                        singleTransformedDataDescriptor(data2, metadata2) +
                        singleTransformedDataDescriptor(data3, metadata3)
            )
        ) {
            descriptors shouldHaveSize 3

            with(descriptors[0]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data shouldBe data2
                this.metadata shouldBe metadata2
            }
            data2.exists() shouldBe true

            with(descriptors[1]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data.getBytes() shouldBe dataContent
                this.metadata shouldBe metadata
            }

            with(descriptors[2]) {
                this.data shouldBe instanceOf(FileData::class)
                this.data.getBytes() shouldBe dataContent3
                this.metadata shouldBe metadata3
            }
            data3.exists() shouldBe false
        }
    }

    private fun createFileData(byteArray: ByteArray, directory: File? = null): FileData =
        createTempFile(directory = directory).apply {
            writeBytes(byteArray)
        }.toFileData()
}