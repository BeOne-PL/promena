package pl.beone.promena.communication.external.memory.internal

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
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryWithBackPressureIncomingExternalCommunicationConverterTest {

    companion object {
        private val communicationParameters = MapCommunicationParameters.create("memory")
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryWithBackPressureIncomingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ the same id communication parameter`() {
        val dataDescriptors = listOf(DataDescriptor("test".createInMemoryData(), TEXT_PLAIN, MapMetadata.empty()))

        MemoryWithBackPressureIncomingExternalCommunicationConverter()
                .convert(dataDescriptors, communicationParameters, communicationParameters) shouldBe dataDescriptors
    }

    @Test
    fun `convert _ should convert Data to InMemoryData`() {
        val bytes = "converted test".toByteArray()
        val mediaType = TEXT_PLAIN

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }

        val dataDescriptors = listOf(DataDescriptor(data, mediaType, MapMetadata(mapOf("key" to "value"))))

        val internalCommunicationParameters = MapCommunicationParameters.create("different")

        MemoryWithBackPressureIncomingExternalCommunicationConverter()
                .convert(dataDescriptors, communicationParameters, internalCommunicationParameters).let {
                    it shouldHaveSize 1

                    val dataDescriptor = it.first()
                    dataDescriptor.data should instanceOf(InMemoryData::class)
                    dataDescriptor.data.getBytes() shouldBe bytes
                    dataDescriptor.mediaType shouldBe mediaType
                }
    }

    @Test
    fun `convert _ delete throws DataDeleteException _ should convert Data to InMemoryData`() {
        val bytes = "converted test".toByteArray()
        val mediaType = TEXT_PLAIN

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } throws DataDeleteException("Exception")
        }

        val dataDescriptors = listOf(DataDescriptor(data, mediaType, MapMetadata(mapOf("key" to "value"))))

        val internalCommunicationParameters = MapCommunicationParameters.create("different")

        MemoryWithBackPressureIncomingExternalCommunicationConverter()
                .convert(dataDescriptors, communicationParameters, internalCommunicationParameters).let {
                    it shouldHaveSize 1

                    val dataDescriptor = it.first()
                    dataDescriptor.data should instanceOf(InMemoryData::class)
                    dataDescriptor.data.getBytes() shouldBe bytes
                    dataDescriptor.mediaType shouldBe mediaType
                }
    }

}