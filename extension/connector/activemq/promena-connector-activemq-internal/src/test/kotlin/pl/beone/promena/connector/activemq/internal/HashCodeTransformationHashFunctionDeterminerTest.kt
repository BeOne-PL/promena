package pl.beone.promena.connector.activemq.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class HashCodeTransformationHashFunctionDeterminerTest {

    companion object {
        private val transformationHashFunctionDeterminer = HashCodeTransformationHashFunctionDeterminer()
    }

    @Test
    fun determine() {
        val hashCode = "-1922677359"

        val converterTransformerId = "converter".toTransformerId()
        val libreOfficeConverterTransformerId = ("converter" to "libreoffice").toTransformerId()
        val barcodeTransformerId = "barcode".toTransformerId()
        val zxingBarcodeTransformerId = ("barcode" to "zxing").toTransformerId()

        transformationHashFunctionDeterminer.determine(
            listOf(converterTransformerId, libreOfficeConverterTransformerId, barcodeTransformerId, zxingBarcodeTransformerId)
        ) shouldBe hashCode
        transformationHashFunctionDeterminer.determine(
            listOf(zxingBarcodeTransformerId, barcodeTransformerId, libreOfficeConverterTransformerId, converterTransformerId)
        ) shouldBe hashCode
    }

    @Test
    fun `determine _ different order`() {
        transformationHashFunctionDeterminer.determine(listOf("converter".toTransformerId())) shouldNotBe
                transformationHashFunctionDeterminer.determine(listOf(("converter" to "libreoffice").toTransformerId()))
    }
}