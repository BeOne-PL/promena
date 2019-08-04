package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import org.junit.Test

class NoDataDslTest {

    @Test
    fun noData_() {
        noData() shouldBe NoData
    }
}
