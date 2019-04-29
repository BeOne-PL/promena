package pl.beone.promena.communication.file.internal

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            assertThat(it).isEqualTo(dataDescriptor)
        }
    }

    @Test
    fun `convert _ data location isn't contained in location _ should copy file`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        val dataDescriptor = createFileTransformedDataDescriptor(createTempDir())

        converter.convert(dataDescriptor, MapCommunicationParameters.empty()).let {
            assertThat(it).isNotEqualTo(dataDescriptor)
            assertThat(it.data.getBytes()).isEqualTo(dataDescriptor.data.getBytes())
        }
    }

    @Test
    fun `convert _ data location scheme is different _ should throw CommunicationException`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        val data = mock<Data> {
            on { getLocation() } doReturn URI("http://noMatter.com")
        }

        assertThatThrownBy {
            converter.convert(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN),
                              MapCommunicationParameters.empty())
        }
                .isExactlyInstanceOf(CommunicationException::class.java)
                .hasMessage("Data location <http://noMatter.com> hasn't <file> scheme")
    }

    @Test
    fun `convert _ not FileData implementation _ should throw CommunicationException`() {
        val converter = FileIncomingCommunicationConverter(createTempDir().toURI())

        assertThatThrownBy {
            converter.convert(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.TEXT_PLAIN),
                              MapCommunicationParameters.empty())
        }
                .isExactlyInstanceOf(CommunicationException::class.java)
                .hasMessage("Data exists only in memory but should be file")
    }

    private fun createFileTransformedDataDescriptor(directory: File? = null): DataDescriptor =
            DataDescriptor(FileData(createTempFile(directory = directory).apply { writeText("test") }.toURI()),
                           MediaTypeConstants.TEXT_PLAIN)
}