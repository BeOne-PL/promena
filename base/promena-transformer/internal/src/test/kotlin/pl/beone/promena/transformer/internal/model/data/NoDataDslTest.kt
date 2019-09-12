package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class NoDataDslTest {

    @Test
    fun noData_() {
        noData() shouldBe NoData
    }
}
