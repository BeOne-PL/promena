package pl.beone.promena.communication.external.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryIncomingExternalCommunicationConverterTest {

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryIncomingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun convert() {
        val data = "test".toMemoryData()
        val metadata = MapMetadata.empty()
        val data2 = mockk<Data>()
        val metadata2 = MapMetadata(mapOf("key" to "value"))
        val dataDescriptors = listOf(DataDescriptor(data, TEXT_PLAIN, metadata), DataDescriptor(data2, APPLICATION_PDF, metadata2))
        val convertedDataDescriptor2 = mockk<Data>()
        val transformedDataDescriptors =
                listOf(TransformedDataDescriptor(data, metadata), TransformedDataDescriptor(convertedDataDescriptor2, metadata2))
        val externalCommunicationParameters = MapCommunicationParameters.empty()
        val convertedDataDescriptors =
                listOf(DataDescriptor(data, TEXT_PLAIN, metadata), DataDescriptor(convertedDataDescriptor2, APPLICATION_PDF, metadata2))

        val internalCommunicationConverter = mockk<InternalCommunicationConverter> {
            every { convert(dataDescriptors, emptyList()) } returns transformedDataDescriptors
        }

        MemoryIncomingExternalCommunicationConverter(internalCommunicationConverter)
                .convert(dataDescriptors, externalCommunicationParameters) shouldBe convertedDataDescriptors
    }

    private fun String.toMemoryData(): MemoryData =
            MemoryData(this.toByteArray())

}