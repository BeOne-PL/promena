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

        with(
            MemoryDescriptorConverter.convert(
                singleDataDescriptor(data, mediaType, metadata) + singleDataDescriptor(data2, mediaType2, metadata2)
            )
        ) {
            descriptors shouldHaveSize 2

            with(descriptors[0]) {
                this.data shouldBe instanceOf(MemoryData::class)
                this.data shouldBe data2
                this.mediaType shouldBe mediaType2
                this.metadata shouldBe metadata2
            }

            with(descriptors[1]) {
                this.data shouldBe instanceOf(MemoryData::class)
                this.data.getBytes() shouldBe dataBytes
                this.mediaType shouldBe mediaType
                this.metadata shouldBe metadata
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

        with(
            MemoryDescriptorConverter.convert(
                singleTransformedDataDescriptor(data, metadata) + singleTransformedDataDescriptor(data2, metadata2)
            )
        ) {
            descriptors shouldHaveSize 2

            with(descriptors[0]) {
                this.data shouldBe instanceOf(MemoryData::class)
                this.data shouldBe data2
                this.metadata shouldBe metadata2
            }

            with(descriptors[1]) {
                this.data shouldBe instanceOf(MemoryData::class)
                this.data.getBytes() shouldBe dataBytes
                this.metadata shouldBe metadata
            }
        }
    }
}