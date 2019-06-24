package pl.beone.promena.communication.file.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationException
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import java.io.File
import java.net.URI

class FileIncomingCommunicationConverterTest {

    @Test
    fun `convert _ data location is contained in location _ should do nothing`() {
        val directory = createTempDir()

        val converter = FileIncomingCommunicationConverter(directory.toURI())

        val dataDescriptor = createFileTransformedDataDescriptor(directory)

        converter.convert(dataDescriptor, MapCommunicationParameters.empty()).let {
            dataDescriptor shouldBe it
        }
    }

    @Test
    fun `convert _ data location isn't contained in location _ should copy file`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        val dataDescriptor = createFileTransformedDataDescriptor(createTempDir())

        converter.convert(dataDescriptor, MapCommunicationParameters.empty()).let {
            it shouldBe dataDescriptor
            it.data.getBytes() shouldBe dataDescriptor.data.getBytes()
        }
    }

    @Test
    fun `convert _ data location scheme is different _ should throw CommunicationException`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        val data = mockk<Data> {
            every { getLocation() } returns URI("http://noMatter.com")
        }

        val dataDescriptor = DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)

        shouldThrow<CommunicationException> {
            converter.convert(dataDescriptor, MapCommunicationParameters.empty())
        }.message shouldBe "Data location <http://noMatter.com> hasn't <file> scheme"
    }

    @Test
    fun `convert _ not FileData implementation _ should throw CommunicationException`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        val dataDescriptor = DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN)

        shouldThrow<CommunicationException> {
            converter.convert(dataDescriptor, MapCommunicationParameters.empty())
        }.message shouldBe "Data exists only in memory but should be file"
    }

    private fun createFileTransformedDataDescriptor(directory: File? = null): DataDescriptor =
            DataDescriptor(FileData(createTempFile(directory = directory).apply { writeText("test") }.toURI()), MediaTypeConstants.TEXT_PLAIN)
}