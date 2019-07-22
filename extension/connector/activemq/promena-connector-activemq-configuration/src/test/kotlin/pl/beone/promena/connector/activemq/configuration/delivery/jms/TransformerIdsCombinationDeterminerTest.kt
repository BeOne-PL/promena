package pl.beone.promena.connector.activemq.configuration.delivery.jms

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test

import org.junit.Assert.*

class TransformerIdsCombinationDeterminerTest {

    companion object {
        private val transformerIdsCombinationDeterminer = TransformerIdsCombinationDeterminer()
    }

    @Test
    fun determine() {
        transformerIdsCombinationDeterminer.determine(listOf("barcode", "converter", "jasper")).let {
            it shouldHaveSize 7

            it shouldContain listOf("barcode")
            it shouldContain listOf("converter")
            it shouldContain listOf("jasper")

            it shouldContain listOf("barcode", "converter")
            it shouldContain listOf("barcode", "jasper")
            it shouldContain listOf("converter", "jasper")

            it shouldContain listOf("barcode", "converter", "jasper")
        }
    }
}