package pl.beone.promena.communication.internal.memory.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata

class MemoryInternalCommunicationConverterTest {

    companion object {
        private val mediaType = TEXT_PLAIN
        private val metadata = MapMetadata(mapOf("key" to "value"))
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.internal.memory.internal.MemoryInternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ all MemoryData instances _ should do nothing`() {
        val dataDescriptors = listOf(DataDescriptor("test".createMemoryData(), mediaType, metadata),
                                     DataDescriptor("test2".createMemoryData(), mediaType, metadata))
        val transformedDataDescriptors = listOf(TransformedDataDescriptor("test".createMemoryData(), metadata),
                                                TransformedDataDescriptor("test2".createMemoryData(), metadata))

        MemoryInternalCommunicationConverter()
                .convert(dataDescriptors, transformedDataDescriptors) shouldBe transformedDataDescriptors
    }

    @Test
    fun `convert __ should convert Data to MemoryData and delete old data resource`() {
        val bytes = "test".toByteArray()
        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }
        val transformedDataDescriptors =
                listOf(TransformedDataDescriptor(data, metadata), TransformedDataDescriptor("test2".createMemoryData(), metadata))

        MemoryInternalCommunicationConverter()
                .convert(emptyList(), transformedDataDescriptors).let {
                    it shouldHaveSize 2

                    val transformedDataDescriptor = it[1]
                    transformedDataDescriptor.data should instanceOf(MemoryData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe bytes
                    transformedDataDescriptor.metadata shouldBe metadata

                    it[0] shouldBe transformedDataDescriptors[1]
                }

        verify { data.getBytes() }
        verify { data.delete() }
    }

    @Test
    fun `convert _ delete throws DataDeleteException _ should convert Data to MemoryData and handle delete exception`() {
        val bytes = "test".toByteArray()
        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } throws DataDeleteException("Exception")
        }
        val transformedDataDescriptors =
                listOf(TransformedDataDescriptor(data, metadata), TransformedDataDescriptor("test2".createMemoryData(), metadata))

        MemoryInternalCommunicationConverter()
                .convert(emptyList(), transformedDataDescriptors).let {
                    it shouldHaveSize 2

                    val transformedDataDescriptor = it[1]
                    transformedDataDescriptor.data should instanceOf(MemoryData::class)
                    transformedDataDescriptor.data.getBytes() shouldBe bytes
                    transformedDataDescriptor.metadata shouldBe metadata

                    it[0] shouldBe transformedDataDescriptors[1]
                }

        verify { data.getBytes() }
        verify { data.delete() }
    }

    @Test
    fun `convert __ should delete data descriptor resources`() {
        val data = mockk<Data> {
            every { delete() } just Runs
        }
        val dataDescriptors = listOf(DataDescriptor("test".createMemoryData(), mediaType, metadata),
                                     DataDescriptor(data, mediaType, metadata))

        MemoryInternalCommunicationConverter()
                .convert(dataDescriptors, emptyList()).let {
                    it shouldHaveSize 0
                }

        verify { data.delete() }
    }

    private fun String.createMemoryData(): MemoryData =
            MemoryData(toByteArray())
}