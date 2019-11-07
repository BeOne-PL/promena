package pl.beone.promena.communication.memory.model.common.converter

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.memory.MemoryData
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData

class MemoryDescriptorConverterTest {

    @Test
    fun `convert _ dataDescriptor`() {
        val dataBytes = "data".toByteArray()
        val data = mockk<Data> {
            every { getBytes() } returns dataBytes
            every { delete() } just Runs
        }
        val mediaType = TEXT_PLAIN
        val metadata = mockk<Metadata>()

        val data2 = "".toMemoryData()
        val mediaType2 = APPLICATION_PDF
        val metadata2 = mockk<Metadata>()

        MemoryDescriptorConverter.convert(
            singleDataDescriptor(data, mediaType, metadata) +
                    singleDataDescriptor(data2, mediaType2, metadata2)
        ).let {
            it.descriptors shouldHaveSize 2

            it.descriptors[0].let { dataDescriptor ->
                dataDescriptor.data shouldBe instanceOf(MemoryData::class)
                dataDescriptor.data shouldBe data2
                dataDescriptor.mediaType shouldBe mediaType2
                dataDescriptor.metadata shouldBe metadata2
            }

            it.descriptors[1].let { dataDescriptor ->
                dataDescriptor.data shouldBe instanceOf(MemoryData::class)
                dataDescriptor.data.getBytes() shouldBe dataBytes
                dataDescriptor.mediaType shouldBe mediaType
                dataDescriptor.metadata shouldBe metadata
            }
        }
    }

    @Test
    fun `convert _ transformedDataDescriptor`() {
        val dataBytes = "data".toByteArray()
        val data = mockk<Data> {
            every { getBytes() } returns dataBytes
            every { delete() } just Runs
        }
        val metadata = mockk<Metadata>()

        val data2 = "".toMemoryData()
        val metadata2 = mockk<Metadata>()

        MemoryDescriptorConverter.convert(
            singleTransformedDataDescriptor(data, metadata) +
                    singleTransformedDataDescriptor(data2, metadata2)
        ).let {
            it.descriptors shouldHaveSize 2

            it.descriptors[0].let { transformedDataDescriptor ->
                transformedDataDescriptor.data shouldBe instanceOf(MemoryData::class)
                transformedDataDescriptor.data shouldBe data2
                transformedDataDescriptor.metadata shouldBe metadata2
            }

            it.descriptors[1].let { transformedDataDescriptor ->
                transformedDataDescriptor.data shouldBe instanceOf(MemoryData::class)
                transformedDataDescriptor.data.getBytes() shouldBe dataBytes
                transformedDataDescriptor.metadata shouldBe metadata
            }
        }
    }
}