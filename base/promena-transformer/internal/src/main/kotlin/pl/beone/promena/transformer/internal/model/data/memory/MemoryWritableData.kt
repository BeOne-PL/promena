package pl.beone.promena.transformer.internal.model.data.memory

import com.cedarsoftware.util.FastByteArrayOutputStream
import pl.beone.promena.transformer.contract.model.data.WritableData
import java.io.OutputStream

class MemoryWritableData internal constructor(
    fastByteArrayOutputStream: FastByteArrayOutputStream
) : WritableData, MemoryData(fastByteArrayOutputStream) {

    companion object {
        @JvmStatic
        fun empty(): MemoryWritableData =
            MemoryWritableData(FastByteArrayOutputStream(0))
    }

    override fun getOutputStream(): OutputStream =
        fastByteArrayOutputStream

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemoryWritableData) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}