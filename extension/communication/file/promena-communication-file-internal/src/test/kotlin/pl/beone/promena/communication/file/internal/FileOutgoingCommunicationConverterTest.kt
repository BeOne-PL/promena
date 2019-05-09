package pl.beone.promena.communication.file.internal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationException
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.io.File
import java.net.URI

class FileOutgoingCommunicationConverterTest {

    @Test
    fun `convert _ data location isn't contained in location _ should copy file`() {
        val directory = createTempDir()

        val converter = FileOutgoingCommunicationConverter()

        val dataDescriptor = createFileTransformedDataDescriptor(createTempDir())

        converter.convert(dataDescriptor,
                          createCommunicationParametersWithLocation(directory.toURI())).let {
            assertThat(it).isNotEqualTo(dataDescriptor)
            assertThat(it.data.getBytes()).isEqualTo(dataDescriptor.data.getBytes())
        }
    }

    @Test
    fun `convert _ data location scheme is different _ should throw CommunicationException`() {
        val converter = FileOutgoingCommunicationConverter()

        assertThatThrownBy {
            converter.convert(createFileTransformedDataDescriptor(createTempDir()),
                              createCommunicationParametersWithLocation(URI("http://noMatter.com")))
        }
                .isExactlyInstanceOf(CommunicationException::class.java)
                .hasMessage("Location <http://noMatter.com> hasn't <file> scheme")
    }

    private fun createCommunicationParametersWithLocation(location: URI): MapCommunicationParameters =
            MapCommunicationParameters(mapOf("location" to location.toString()))

    private fun createFileTransformedDataDescriptor(directory: File? = null): TransformedDataDescriptor =
            TransformedDataDescriptor(FileData(createTempFile(directory = directory).apply { writeText("test") }.toURI()),
                                      MapMetadata.empty())

}
