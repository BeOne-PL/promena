package pl.beone.promena.communication.internal.file.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.io.File
import java.net.URI

class FileInternalCommunicationConverterTest {

    companion object {
        private val location = createTempDir().toURI()
        private val internalCommunicationParameters = MapCommunicationParameters.create("file", mapOf("location" to location))
        private val metadata = MapMetadata(mapOf("key" to "value"))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.internal.file.internal.FileInternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ FileData instance `() {
        val transformedDataDescriptors = listOf(TransformedDataDescriptor("test".toFileData(location), metadata))

        FileInternalCommunicationConverter(internalCommunicationParameters)
                .convert(emptyList(), transformedDataDescriptors) shouldBe transformedDataDescriptors
    }

    @Test
    fun `convert _ should convert Data to FileData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        FileInternalCommunicationConverter(internalCommunicationParameters)
                .convert(emptyList(), transformedDataDescriptors).let {
                    it shouldHaveSize 1

                    val transformedDataDescriptor = it.first()
                    transformedDataDescriptor.data should instanceOf(FileData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe bytes
                    transformedDataDescriptor.metadata shouldBe metadata
                }
    }

    @Test
    fun `convert _ delete throws DataDeleteException _ should convert Data to FileData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } throws DataDeleteException("Exception")
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        FileInternalCommunicationConverter(internalCommunicationParameters)
                .convert(emptyList(), transformedDataDescriptors).let {
                    it shouldHaveSize 1

                    val dataDescriptor = it.first()
                    dataDescriptor.data should instanceOf(FileData::class)
                    dataDescriptor.data.getBytes() shouldBe bytes
                    dataDescriptor.metadata shouldBe metadata
                }
    }

    @Test
    fun `convert _ FileData but in different location _ should convert new FileData`() {
        val text = "converted test"

        val fileData = text.toFileData(createTempDir().toURI())

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(fileData, metadata))

        FileInternalCommunicationConverter(internalCommunicationParameters)
                .convert(emptyList(), transformedDataDescriptors).let {
                    it shouldHaveSize 1

                    val transformedDataDescriptor = it.first()
                    transformedDataDescriptor.data should instanceOf(FileData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe text.toByteArray()
                    transformedDataDescriptor.metadata shouldBe metadata

                    File(fileData.getLocation()).exists() shouldBe false
                }
    }

    @Test
    fun `convert _ _ remove no longer use files from data descriptors`() {
        val differentData = mockk<Data> {
            every { getBytes() } returns "no matter".toByteArray()
            every { delete() } just Runs
        }
        val commonData = "test".toFileData(location)

        val deletedData = "deleted data".toFileData(location)
        val notDeletedData = "not deleted data".toFileData(location)

        val dataDescriptors = listOf(DataDescriptor(differentData, TEXT_PLAIN, metadata),
                                     DataDescriptor(commonData, TEXT_PLAIN, metadata),
                                     DataDescriptor(deletedData, TEXT_PLAIN, metadata))

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(differentData, metadata),
                                                TransformedDataDescriptor(commonData, metadata),
                                                TransformedDataDescriptor(notDeletedData, metadata))

        FileInternalCommunicationConverter(internalCommunicationParameters).convert(dataDescriptors, transformedDataDescriptors)
        commonData.exists() shouldBe true
        deletedData.exists() shouldBe false
        notDeletedData.exists() shouldBe true
    }

    private fun String.toFileData(location: URI): FileData =
            FileData(createTempFile(directory = File(location)).apply {
                writeText(this@toFileData)
            }.toURI())

    private fun Data.exists(): Boolean =
            File(getLocation()).exists()
}