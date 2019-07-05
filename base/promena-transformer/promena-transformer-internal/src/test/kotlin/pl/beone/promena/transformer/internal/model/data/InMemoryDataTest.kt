package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import org.junit.Test

class InMemoryDataTest {

    companion object {
        private val bytes = "test".toByteArray()
    }

    @Test
    fun getBytes() {
        InMemoryData(bytes).getBytes() shouldBe bytes
    }

    @Test
    fun `getLocation _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> { InMemoryData(bytes).getLocation() }
                .message shouldBe "This resource exists only in memory"
    }

    @Test
    fun isAvailable() {
        shouldNotThrowAny { InMemoryData(bytes).isAccessible() }
    }

    @Test
    fun delete() {
        shouldThrow<UnsupportedOperationException> {
            InMemoryData(bytes).delete()
        }.message shouldBe "This resource exists only in memory"
    }

    @Test
    fun equals() {
        InMemoryData(bytes) shouldBe InMemoryData("test".toByteArray())
    }
}