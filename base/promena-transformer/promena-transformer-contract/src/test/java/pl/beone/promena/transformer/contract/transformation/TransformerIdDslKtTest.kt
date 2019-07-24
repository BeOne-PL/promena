package pl.beone.promena.transformer.contract.transformation

import io.kotlintest.shouldBe
import org.junit.Test

class TransformerIdDslKtTest {

    @Test
    fun transformerId() {
        pl.beone.promena.transformer.contract.transformer.transformerId("transfomer", "libreoffice").let {
            it.name shouldBe "transfomer"
            it.subName shouldBe "libreoffice"
        }
    }

    @Test
    fun `transformerId _ null implementation names`() {
        pl.beone.promena.transformer.contract.transformer.transformerId("transfomer").let {
            it.name shouldBe "transfomer"
            it.subName shouldBe null
        }
    }
}