package pl.beone.promena.transformer.contract.data

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

class TransformedDataDescriptorDslTest {

    companion object {
        private val data = mockk<Data>()
        private val metadata = mockk<Metadata>()

        private val data2 = mockk<Data>()
        private val metadata2 = mockk<Metadata>()

        private val singleTransformedDataDescriptor = TransformedDataDescriptor.Single.of(data, metadata)
        private val singleTransformedDataDescriptor2 = TransformedDataDescriptor.Single.of(data2, metadata2)
    }

    @Test
    fun emptyTransformedDataDescriptor_() {
        emptyTransformedDataDescriptor() shouldBe
                TransformedDataDescriptor.Empty
    }

    @Test
    fun `plus _ empty data descriptor`() {
        emptyTransformedDataDescriptor() + singleTransformedDataDescriptor shouldBe
                singleTransformedDataDescriptor
    }

    @Test
    fun singleTransformedDataDescriptor() {
        singleTransformedDataDescriptor shouldBe
                singleTransformedDataDescriptor
    }

    @Test
    fun `plus _ single transformed data descriptor`() {
        singleTransformedDataDescriptor + singleTransformedDataDescriptor2 shouldBe
                TransformedDataDescriptor.Multi.of(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2))
    }

    @Test
    fun multiTransformedDataDescriptor() {
        multiTransformedDataDescriptor(singleTransformedDataDescriptor, listOf(singleTransformedDataDescriptor2)) shouldBe
                TransformedDataDescriptor.Multi.of(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2))

        multiTransformedDataDescriptor(singleTransformedDataDescriptor, singleTransformedDataDescriptor2) shouldBe
                TransformedDataDescriptor.Multi.of(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2))
    }

    @Test
    fun `plus _ multi data descriptor`() {
        multiTransformedDataDescriptor(singleTransformedDataDescriptor, singleTransformedDataDescriptor2) + singleTransformedDataDescriptor shouldBe
                TransformedDataDescriptor.Multi.of(
                    listOf(
                        singleTransformedDataDescriptor,
                        singleTransformedDataDescriptor2,
                        singleTransformedDataDescriptor
                    )
                )
    }

    @Test
    fun `transformedDataDescriptor _ zero transformed data descriptors - Empty`() {
        transformedDataDescriptor() shouldBe
                TransformedDataDescriptor.Empty
    }

    @Test
    fun `transformedDataDescriptor _ one single transformed data descriptors - Single`() {
        transformedDataDescriptor(singleTransformedDataDescriptor) shouldBe
                TransformedDataDescriptor.Single.of(data, metadata)
    }

    @Test
    fun `transformedDataDescriptor _ two single transformed data descriptors - Multi`() {
        transformedDataDescriptor(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2)) shouldBe
                TransformedDataDescriptor.Multi.of(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2))

        transformedDataDescriptor(singleTransformedDataDescriptor, singleTransformedDataDescriptor2) shouldBe
                TransformedDataDescriptor.Multi.of(listOf(singleTransformedDataDescriptor, singleTransformedDataDescriptor2))
    }

    @Test
    fun toTransformedDataDescriptor() {
        listOf(singleTransformedDataDescriptor).toTransformedDataDescriptor() shouldBe
                TransformedDataDescriptor.Single.of(data, metadata)
    }
}