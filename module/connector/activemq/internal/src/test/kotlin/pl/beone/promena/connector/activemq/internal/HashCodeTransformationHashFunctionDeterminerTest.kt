package pl.beone.promena.connector.activemq.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class HashCodeTransformationHashFunctionDeterminerTest {

    @Test
    fun determine() {
        val hashCode = "-1922677359"

        val converterTransformerId = "converter".toTransformerId()
        val libreOfficeConverterTransformerId = ("converter" to "libreoffice").toTransformerId()
        val barcodeTransformerId = "barcode".toTransformerId()
        val zxingBarcodeTransformerId = ("barcode" to "zxing").toTransformerId()

        HashCodeTransformationHashFunctionDeterminer.determine(
            listOf(converterTransformerId, libreOfficeConverterTransformerId, barcodeTransformerId, zxingBarcodeTransformerId)
        ) shouldBe hashCode
        HashCodeTransformationHashFunctionDeterminer.determine(
            listOf(zxingBarcodeTransformerId, barcodeTransformerId, libreOfficeConverterTransformerId, converterTransformerId)
        ) shouldBe hashCode
    }

    @Test
    fun `determine _ different order`() {
        HashCodeTransformationHashFunctionDeterminer.determine(listOf("converter".toTransformerId())) shouldNotBe
                HashCodeTransformationHashFunctionDeterminer.determine(listOf(("converter" to "libreoffice").toTransformerId()))
    }
}