package pl.beone.promena.communication.file.internal

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import java.net.URI

class FileInternalCommunicationConverterTest {

    @Test
    fun `convert _ not FileData implementation _ should save in file`() {
        val converter = FileInternalCommunicationConverter(createTempDir().toURI())

        val data = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
            every { getBytes() } returns "test".toByteArray()
        }

        converter.convert(TransformedDataDescriptor(data, MapMetadata.empty())).let {
            it.data.getBytes() shouldBe "test".toByteArray()
            it.data.getLocation()
        }
    }

    @Test
    fun `convert _ data location scheme is different _ should save in file`() {
        val converter = FileInternalCommunicationConverter(createTempDir().toURI())

        val data = mockk<Data> {
            every { getLocation() } returns URI("http://noMatter.com")
            every { getBytes() } returns "test".toByteArray()
        }

        converter.convert(TransformedDataDescriptor(data, MapMetadata.empty())).let {
            it.data.getBytes() shouldBe "test".toByteArray()
            it.data.getLocation()
        }
    }

}