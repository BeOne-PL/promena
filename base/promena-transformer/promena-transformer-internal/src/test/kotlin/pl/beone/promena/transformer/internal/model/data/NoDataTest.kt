package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class NoDataTest {

    @Test
    fun getBytes() {
        verify { NoData.getBytes() }
    }

    @Test
    fun getInputStream() {
        verify { NoData.getInputStream() }
    }

    @Test
    fun getLocation() {
        verify { NoData.getLocation() }
    }

    @Test
    fun isAccessible() {
        verify { NoData.isAccessible() }
    }

    @Test
    fun delete() {
        verify { NoData.delete() }
    }

    private fun verify(toVerify: () -> Unit) {
        shouldThrow<UnsupportedOperationException> {
            toVerify()
        }.message shouldBe "This resource has no content"
    }
}
