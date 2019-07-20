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
import pl.beone.promena.transformer.contract.data.*
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus

class MemoryInternalCommunicationConverterTest {

    companion object {
        private val mediaType = TEXT_PLAIN
        private val metadata = emptyMetadata() + ("key" to "value")
    }

    @Before
    fun setUp() {
        (LoggerFactory.getLogger("pl.beone.promena.communication.internal.memory.internal.MemoryInternalCommunicationConverter") as Logger)
                .level = Level.DEBUG
    }

    @Test
    fun `convert _ all MemoryData instances _ should do nothing`() {
        val transformedDataDescriptor =
                singleTransformedDataDescriptor("test".toMemoryData(), metadata) + singleTransformedDataDescriptor("test2".toMemoryData(), metadata)

        MemoryInternalCommunicationConverter()
                .convert(singleDataDescriptor("test".toMemoryData(), mediaType, metadata) +
                                 singleDataDescriptor("test2".toMemoryData(), mediaType, metadata),
                         transformedDataDescriptor) shouldBe transformedDataDescriptor
    }

    @Test
    fun `convert __ should convert Data to MemoryData and delete old data resource`() {
        val bytes = "test".toByteArray()
        val data = mockk<Data> {
            every { getBytes() } returns bytes
            every { delete() } just Runs
        }
        val transformedDataDescriptor =
                singleTransformedDataDescriptor(data, metadata) + singleTransformedDataDescriptor("test2".toMemoryData(), metadata)

        MemoryInternalCommunicationConverter()
                .convert(emptyDataDescriptor(), transformedDataDescriptor)
                .let { resultTransformedDataDescriptor ->
                    val descriptors = resultTransformedDataDescriptor.descriptors
                    descriptors shouldHaveSize 2

                    descriptors[1].let { singleTransformedDataDescriptor ->
                        singleTransformedDataDescriptor.data should instanceOf(MemoryData::class)
                        singleTransformedDataDescriptor.data.getBytes() shouldBe bytes
                        singleTransformedDataDescriptor.metadata shouldBe metadata
                    }

                    descriptors[0] shouldBe transformedDataDescriptor.descriptors[1]
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
        val transformedDataDescriptor =
                singleTransformedDataDescriptor(data, metadata) + singleTransformedDataDescriptor("test2".toMemoryData(), metadata)

        MemoryInternalCommunicationConverter()
                .convert(emptyDataDescriptor(), transformedDataDescriptor)
                .let { resultTransformedDataDescriptor ->
                    val descriptors = resultTransformedDataDescriptor.descriptors
                    descriptors shouldHaveSize 2

                    descriptors[1].let { singleTransformedDataDescriptor ->
                        singleTransformedDataDescriptor.data should instanceOf(MemoryData::class)
                        singleTransformedDataDescriptor.data.getBytes() shouldBe bytes
                        singleTransformedDataDescriptor.metadata shouldBe metadata
                    }

                    descriptors[0] shouldBe transformedDataDescriptor.descriptors[1]
                }

        verify { data.getBytes() }
        verify { data.delete() }
    }

    @Test
    fun `convert __ should delete data descriptor resources`() {
        val data = mockk<Data> {
            every { delete() } just Runs
        }
        val dataDescriptor = singleDataDescriptor("test".toMemoryData(), mediaType, metadata) + singleDataDescriptor(data, mediaType, metadata)

        MemoryInternalCommunicationConverter()
                .convert(dataDescriptor, emptyTransformedDataDescriptor()).descriptors shouldHaveSize 0

        verify { data.delete() }
    }

}