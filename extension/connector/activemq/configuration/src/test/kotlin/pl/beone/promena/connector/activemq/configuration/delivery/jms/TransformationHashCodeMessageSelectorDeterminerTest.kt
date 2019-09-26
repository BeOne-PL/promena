package pl.beone.promena.connector.activemq.configuration.delivery.jms

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class TransformationHashCodeMessageSelectorDeterminerTest {

    @Test
    fun determine() {
        val converterTransformerId = "converter".toTransformerId()
        val converterTransformer = mockk<Transformer>()
        val barcodeTransformerId = "barcode".toTransformerId()
        val barcodeTransformer = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformerId(converterTransformer) } returns converterTransformerId
            every { getTransformerId(barcodeTransformer) } returns barcodeTransformerId
        }

        val transformationHashFunctionDeterminer = mockk<TransformationHashFunctionDeterminer> {
            every { determine(listOf(converterTransformerId)) } returns "1"
            every { determine(listOf(barcodeTransformerId)) } returns "2"
            every { determine(listOf(converterTransformerId, barcodeTransformerId)) } returns "3"
        }

        TransformationHashCodeMessageSelectorDeterminer.determine(
            transformerConfig,
            listOf(converterTransformer, barcodeTransformer),
            transformationHashFunctionDeterminer
        ) shouldBe
                "$TRANSFORMATION_HASH_CODE IN ('1', '2', '3')"
    }
}