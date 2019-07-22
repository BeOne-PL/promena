package pl.beone.promena.connector.activemq.internal

import io.kotlintest.shouldBe
import org.junit.Test

class HashCodeTransformationHashFunctionDeterminerTest {

    companion object {
        private val transformationHashFunctionDeterminer = HashCodeTransformationHashFunctionDeterminer()
    }

    @Test
    fun determine() {
        val hashCode = "-2100906783"

        transformationHashFunctionDeterminer.determine(listOf("converter", "barcode")) shouldBe hashCode
        transformationHashFunctionDeterminer.determine(listOf("barcode", "converter")) shouldBe hashCode
    }

    @Test
    fun `determine _ different order`() {
        transformationHashFunctionDeterminer.determine(listOf("converter", "barcode", "report")) shouldBe
                transformationHashFunctionDeterminer.determine(listOf("report", "converter", "barcode"))
    }
}