package pl.beone.promena.communication.external.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryIncomingExternalCommunicationConverterTest {

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryIncomingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ the same id communication parameter`() {
        val dataDescriptors = listOf(DataDescriptor("test".toMemoryData(), TEXT_PLAIN, MapMetadata.empty()))
        val externalCommunicationParameters = MapCommunicationParameters.create("memory")
        val internalCommunicationParameters = MapCommunicationParameters.create("file")

        MemoryIncomingExternalCommunicationConverter()
                .convert(dataDescriptors, externalCommunicationParameters, internalCommunicationParameters) shouldBe dataDescriptors
    }
}