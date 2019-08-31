package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.contract.model.Data
import java.io.InputStream
import java.net.URI

data class MemoryData internal constructor(
    private val bytes: ByteArray
) : Data {

    companion object {
        @JvmStatic
        fun of(bytes: ByteArray): MemoryData =
            MemoryData(bytes)

        @JvmStatic
        fun of(inputStream: InputStream): MemoryData =
            MemoryData(inputStream.readAllBytes())
    }

    override fun getBytes(): ByteArray =
        bytes

    override fun getInputStream(): InputStream =
        bytes.inputStream()

    override fun getLocation(): URI {
        throw UnsupportedOperationException("This resource exists only in memory")
    }

    override fun isAccessible() {
        // deliberately omitted. Memory is always accessible
    }

    override fun delete() {
        throw UnsupportedOperationException("This resource exists only in memory")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemoryData) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}