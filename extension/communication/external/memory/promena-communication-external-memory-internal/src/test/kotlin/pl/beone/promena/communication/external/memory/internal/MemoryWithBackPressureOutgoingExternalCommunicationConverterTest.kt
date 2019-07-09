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
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryWithBackPressureOutgoingExternalCommunicationConverterTest {

    companion object {
        private val communicationParameters = MapCommunicationParameters.create("memory")
        private val metadata = MapMetadata(mapOf("key" to "value"))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.external.memory.internal.MemoryWithBackPressureOutgoingExternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun convert() {
        val transformedDataDescriptors = listOf(TransformedDataDescriptor("test".createMemoryData(), metadata))

        MemoryWithBackPressureOutgoingExternalCommunicationConverter()
                .convert(transformedDataDescriptors, communicationParameters, communicationParameters) shouldBe transformedDataDescriptors
    }

    @Test
    fun `convert _ should convert Data to MemoryData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        val internalCommunicationParameters = MapCommunicationParameters.create("different")

        MemoryWithBackPressureOutgoingExternalCommunicationConverter()
                .convert(transformedDataDescriptors, communicationParameters, internalCommunicationParameters).let {
                    it shouldHaveSize 1

                    val transformedDataDescriptor = it.first()
                    transformedDataDescriptor.data should instanceOf(MemoryData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe bytes
                    transformedDataDescriptor.metadata shouldBe metadata
                }
    }
}