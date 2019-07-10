package pl.beone.promena.communication.external.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryOutgoingExternalCommunicationConverterTest {

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryOutgoingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun convert() {
        val data = mockk<Data> {
            every { getBytes() } returns "test".toByteArray()
            every { delete() } just Runs
        }
        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, MapMetadata.empty()))
        val externalCommunicationParameters = MapCommunicationParameters.empty()

        MemoryOutgoingExternalCommunicationConverter()
                .convert(transformedDataDescriptors, externalCommunicationParameters)
    }

}