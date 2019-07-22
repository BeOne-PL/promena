package pl.beone.promena.communication.external.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus

class MemoryIncomingExternalCommunicationConverterTest {

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryIncomingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun convert() {
        val data = "test".toMemoryData()
        val metadata = emptyMetadata()

        val data2 = mockk<Data> {
            every { getBytes() } returns "test2".toByteArray()
            every { delete() } just Runs
        }
        val convertedData2 = "test2".toMemoryData()
        val metadata2 = emptyMetadata() + ("key" to "value")

        MemoryIncomingExternalCommunicationConverter()
                .convert(singleDataDescriptor(data, TEXT_PLAIN, metadata) + singleDataDescriptor(data2, APPLICATION_PDF, metadata2),
                         communicationParameters("memory")) shouldBe
                singleDataDescriptor(data, TEXT_PLAIN, metadata) + singleDataDescriptor(convertedData2, APPLICATION_PDF, metadata2)
    }

}