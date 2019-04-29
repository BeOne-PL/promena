package pl.beone.promena.communication.file.internal

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.net.URI

class FileInternalCommunicationConverterTest {

    @Test
    fun `convert _ not FileData implementation _ should save in file`() {
        val converter = FileInternalCommunicationConverter(createTempDir().toURI())

        val data = mock<Data> {
            on { getLocation() } doThrow UnsupportedOperationException()
            on { getBytes() } doReturn "test".toByteArray()
        }

        converter.convert(TransformedDataDescriptor(data, MapMetadata.empty())).let {
            assertThat(it.data.getBytes()).isEqualTo("test".toByteArray())
            it.data.getLocation()
        }
    }

    @Test
    fun `convert _ data location scheme is different _ should save in file`() {
        val converter = FileInternalCommunicationConverter(createTempDir().toURI())

        val data = mock<Data> {
            on { getLocation() } doReturn  URI("http://noMatter.com")
            on { getBytes() } doReturn "test".toByteArray()
        }

        converter.convert(TransformedDataDescriptor(data, MapMetadata.empty())).let {
            assertThat(it.data.getBytes()).isEqualTo("test".toByteArray())
            it.data.getLocation()
        }
    }

}