package pl.beone.promena.transformer.contract.data

import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

class DataDescriptorDslTest {

    companion object {
        private val data = mockk<Data>()
        private val mediaType = TEXT_PLAIN
        private val metadata = mockk<Metadata>()

        private val data2 = mockk<Data>()
        private val mediaType2 = TEXT_XML
        private val metadata2 = mockk<Metadata>()

        private val singleDataDescriptor = DataDescriptor.Single.of(data, mediaType, metadata)
        private val singleDataDescriptor2 = DataDescriptor.Single.of(data2, mediaType2, metadata2)
    }

    @Test
    fun emptyDataDescriptor_() {
        emptyDataDescriptor() shouldBe
                DataDescriptor.Empty
    }

    @Test
    fun `plus _ empty data descriptor`() {
        emptyDataDescriptor() + singleDataDescriptor shouldBe
                singleDataDescriptor
    }

    @Test
    fun singleDataDescriptor() {
        singleDataDescriptor shouldBe
                singleDataDescriptor
    }

    @Test
    fun `plus _ single data descriptor`() {
        singleDataDescriptor + singleDataDescriptor2 shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2))
    }

    @Test
    fun multiDataDescriptor() {
        multiDataDescriptor(singleDataDescriptor, listOf(singleDataDescriptor2)) shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2))

        multiDataDescriptor(singleDataDescriptor, singleDataDescriptor2) shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2))
    }

    @Test
    fun `plus _ multi + single data descriptor`() {
        multiDataDescriptor(singleDataDescriptor, singleDataDescriptor2) + singleDataDescriptor shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2, singleDataDescriptor))
    }

    @Test
    fun `plus _ multi + multi data descriptor`() {
        val multiDataDescriptor = multiDataDescriptor(singleDataDescriptor, singleDataDescriptor2)
        multiDataDescriptor + multiDataDescriptor shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2, singleDataDescriptor, singleDataDescriptor2))
    }

    @Test
    fun `dataDescriptor _ zero data descriptors - Empty`() {
        dataDescriptor() shouldBe
                DataDescriptor.Empty
    }

    @Test
    fun `dataDescriptor _ one single data descriptors - Single`() {
        dataDescriptor(singleDataDescriptor) shouldBe
                DataDescriptor.Single.of(data, mediaType, metadata)
    }

    @Test
    fun `dataDescriptor _ two single data descriptors - Multi`() {
        dataDescriptor(listOf(singleDataDescriptor, singleDataDescriptor2)) shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2))

        dataDescriptor(singleDataDescriptor, singleDataDescriptor2) shouldBe
                DataDescriptor.Multi.of(listOf(singleDataDescriptor, singleDataDescriptor2))
    }

    @Test
    fun toDataDescriptor() {
        listOf(singleDataDescriptor).toDataDescriptor() shouldBe
                DataDescriptor.Single.of(data, mediaType, metadata)
    }
}