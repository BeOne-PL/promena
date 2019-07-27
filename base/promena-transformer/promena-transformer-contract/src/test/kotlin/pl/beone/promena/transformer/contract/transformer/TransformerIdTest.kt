package pl.beone.promena.transformer.contract.transformer

import io.kotlintest.shouldBe
import org.junit.Test

class TransformerIdTest {

    @Test
    fun isSubNameSet() {
        transformerId("converter", "libreoffice").isSubNameSet() shouldBe
                true

        transformerId("converter").isSubNameSet() shouldBe
                false
    }
}