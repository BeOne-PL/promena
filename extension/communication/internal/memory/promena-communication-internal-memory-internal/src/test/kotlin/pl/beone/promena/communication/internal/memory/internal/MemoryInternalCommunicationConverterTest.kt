package pl.beone.promena.communication.internal.memory.internal

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
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryInternalCommunicationConverterTest {

    companion object {
        private val metadata = MapMetadata(mapOf("key" to "value"))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.internal.memory.internal.MemoryInternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun convert() {
        val transformedDataDescriptors = listOf(TransformedDataDescriptor("test".createInMemoryData(), metadata))

        MemoryInternalCommunicationConverter().convert(listOf(), transformedDataDescriptors) shouldBe transformedDataDescriptors
    }

    @Test
    fun `convert _ should convert Data to InMemoryData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        MemoryInternalCommunicationConverter().convert(listOf(), transformedDataDescriptors).let {
            it shouldHaveSize 1

            val transformedDataDescriptor = it.first()
            transformedDataDescriptor.data should instanceOf(InMemoryData::class)
            transformedDataDescriptor.data.getBytes() shouldBe bytes
            transformedDataDescriptor.metadata shouldBe metadata
        }
    }

    @Test
    fun `convert _ delete throws DataDeleteException _ should convert Data to InMemoryData`() {
        val bytes = "converted test".toByteArray()

        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } throws DataDeleteException("Exception")
        }

        val transformedDataDescriptors = listOf(TransformedDataDescriptor(data, metadata))

        MemoryInternalCommunicationConverter().convert(listOf(), transformedDataDescriptors).let {
            it shouldHaveSize 1

            val transformedDataDescriptor = it.first()
            transformedDataDescriptor.data should instanceOf(InMemoryData::class)
            transformedDataDescriptor.data.getBytes() shouldBe bytes
            transformedDataDescriptor.metadata shouldBe metadata
        }
    }

    private fun String.createInMemoryData(): InMemoryData =
            InMemoryData(this.toByteArray())
}