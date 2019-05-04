package pl.beone.promena.transformer.internal.model.data

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class InMemoryDataTest {

    companion object {
        private val bytes = "test".toByteArray()
    }

    @Test
    fun getBytes() {
        InMemoryData(bytes).let {
            assertThat(it.getBytes()).isEqualTo(bytes)
        }
    }

    @Test
    fun `getLocation should throw UnsupportedOperationException`() {
        InMemoryData(bytes).let {
            assertThatThrownBy { it.getLocation() }
                    .isExactlyInstanceOf(UnsupportedOperationException::class.java)
                    .hasMessage("This resource exists only in memory")
        }
    }

    @Test
    fun isAvailable() {
        InMemoryData(bytes).isAvailable()
    }

    @Test
    fun equals() {
        assertThat(InMemoryData(bytes) == InMemoryData("test".toByteArray())).isTrue()
    }
}