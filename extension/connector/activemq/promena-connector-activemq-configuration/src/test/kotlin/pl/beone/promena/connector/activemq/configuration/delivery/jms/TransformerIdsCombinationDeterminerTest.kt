package pl.beone.promena.connector.activemq.configuration.delivery.jms

import org.junit.Test

import org.junit.Assert.*

class TransformerIdsCombinationDeterminerTest {

    @Test
    fun determine() {
        val determine = TransformerIdsCombinationDeterminer().determine(listOf("barcode", "converter", "jasper"))
        val www = "ASD"
    }
}