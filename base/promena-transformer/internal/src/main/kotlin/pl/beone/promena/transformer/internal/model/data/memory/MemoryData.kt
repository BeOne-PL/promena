package pl.beone.promena.transformer.internal.model.data.memory

import com.cedarsoftware.util.FastByteArrayOutputStream
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.util.createFastByteArrayOutputStream
import java.io.InputStream
import java.net.URI

/**
 * The implementation based on memory.
 *
 * @property fastByteArrayOutputStream indicates the resource
 *
 * @see MemoryDataDsl
 */
open class MemoryData internal constructor(
    protected val fastByteArrayOutputStream: FastByteArrayOutputStream
) : Data {

    companion object {
        @JvmStatic
        fun of(bytes: ByteArray): MemoryData =
            MemoryData(createFastByteArrayOutputStream(bytes))

        @JvmStatic
        fun of(inputStream: InputStream): MemoryData =
            MemoryData(createFastByteArrayOutputStream(inputStream.readAllBytes()))
    }

    override fun getBytes(): ByteArray =
        fastByteArrayOutputStream.buffer

    override fun getInputStream(): InputStream =
        fastByteArrayOutputStream.buffer.inputStream()

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

        if (!fastByteArrayOutputStream.buffer!!.contentEquals(other.fastByteArrayOutputStream.buffer)) return false

        return true
    }

    override fun hashCode(): Int {
        return fastByteArrayOutputStream.hashCode()
    }
}