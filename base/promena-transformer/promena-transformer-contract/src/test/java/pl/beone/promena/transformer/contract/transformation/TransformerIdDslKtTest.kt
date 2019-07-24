package pl.beone.promena.transformer.contract.transformation

import io.kotlintest.shouldBe
import org.junit.Test
import pl.beone.promena.transformer.contract.transformer.toTransformerId
import pl.beone.promena.transformer.contract.transformer.transformerId

class TransformerIdDslKtTest {

    @Test
    fun transformerId() {
        transformerId("transfomer", "libreoffice").let {
            it.name shouldBe "transfomer"
            it.subName shouldBe "libreoffice"
        }
    }

    @Test
    fun `transformerId _ null sub name`() {
        transformerId("transfomer").let {
            it.name shouldBe "transfomer"
            it.subName shouldBe null
        }
    }

    @Test
    fun `toTransformerId _ string`() {
        "transfomer".toTransformerId().let {
            it.name shouldBe "transfomer"
            it.subName shouldBe null
        }
    }

    @Test
    fun `toTransformerId _ pair`() {
        ("transfomer" to "libreoffice").toTransformerId().let {
            it.name shouldBe "transfomer"
            it.subName shouldBe "libreoffice"
        }
    }
}