package pl.beone.promena.connector.activemq.configuration.delivery.jms

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class TransformerIdsCombinationDeterminerTest {

    @Test
    fun determine() {
        with(
            TransformerIdsCombinationDeterminer
                .determine(
                    listOf(
                        ("barcode" to "zxing").toTransformerId(),
                        ("converter" to "libreoffice").toTransformerId(),
                        ("converter" to "microsoft-office").toTransformerId()
                    )
                )
        ) {
            this shouldHaveSize 31

            this shouldContain listOf("barcode".toTransformerId())

            this shouldContain listOf(
                "barcode".toTransformerId(),
                ("converter" to "libreoffice").toTransformerId()
            )

            this shouldContain listOf(
                "barcode".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )

            this shouldContain listOf(
                "barcode".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "libreoffice").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )

            this shouldContain listOf(
                "barcode".toTransformerId(),
                "converter".toTransformerId(),
                ("barcode" to "zxing").toTransformerId(),
                ("converter" to "libreoffice").toTransformerId(),
                ("converter" to "microsoft-office").toTransformerId()
            )
        }
    }
}