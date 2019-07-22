package pl.beone.promena.connector.activemq.configuration.delivery.jms

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.internal.HashCodeTransformationHashFunctionDeterminer
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

class TransformationIdMessageSelectorDeterminerTest {

    companion object {
        private val transformationIdMessageSelectorDeterminer = TransformationIdMessageSelectorDeterminer()
    }

    @Test
    fun determine() {
        val converterId = "converter"
        val converterTransformer = mockk<Transformer>()
        val barcodeId = "barcode"
        val barcodeTransformer = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getId(converterTransformer) } returns converterId
            every { getId(barcodeTransformer) } returns barcodeId
        }

        val transformationHashFunctionDeterminer = mockk<TransformationHashFunctionDeterminer> {
            every { determine(listOf(converterId)) } returns "1"
            every { determine(listOf(barcodeId)) } returns "2"
            every { determine(listOf(converterId, barcodeId)) } returns "3"
        }

        transformationIdMessageSelectorDeterminer.determine(transformerConfig,
                                                            listOf(converterTransformer, barcodeTransformer),
                                                            transformationHashFunctionDeterminer) shouldBe
                "${PromenaJmsHeaders.TRANSFORMATION_ID} IN ('1', '2', '3')"
    }
}