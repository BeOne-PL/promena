package pl.beone.promena.connector.activemq.configuration.delivery.jms

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class TransformerIdsCombinationDeterminerTest {

    @Test
    fun determine() {
        TransformerIdsCombinationDeterminer.determine(
            listOf(
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "libreoffice").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )
        ).let {
            it shouldHaveSize 31

            it shouldContain listOf("barcode".toTransformerId())

            it shouldContain listOf(
                "barcode".toTransformerId(),
                ("converter" to "libreoffice").toTransformerId()
            )

            it shouldContain listOf(
                "barcode".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )

            it shouldContain listOf(
                "barcode".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "libreoffice").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )

            it shouldContain listOf(
                "barcode".toTransformerId(),
                "converter".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "libreoffice").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )
        }
    }
}