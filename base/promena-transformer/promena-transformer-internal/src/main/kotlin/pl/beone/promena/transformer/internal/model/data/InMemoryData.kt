package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

data class InMemoryData(private val bytes: ByteArray) : Data {
    override fun getBytes(): ByteArray =
            bytes

    override fun getLocation(): URI {
        throw UnsupportedOperationException("This resource exists only in memory")
    }

    override fun isAccessible() {
    }

    override fun delete() {
        throw UnsupportedOperationException("This resource exists only in memory")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InMemoryData) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}