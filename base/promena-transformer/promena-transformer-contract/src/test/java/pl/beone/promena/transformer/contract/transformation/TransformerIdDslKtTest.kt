package pl.beone.promena.transformer.contract.transformation

import io.kotlintest.shouldBe
import org.junit.Test

class TransformerIdDslKtTest {

    @Test
    fun transformerId() {
        transformerId("transfomer", "libreoffice").let {
            it.name shouldBe "transfomer"
            it.implementationName shouldBe "libreoffice"
        }
    }

    @Test
    fun `transformerId _ null implementation names`() {
        transformerId("transfomer").let {
            it.name shouldBe "transfomer"
            it.implementationName shouldBe null
        }
    }
}