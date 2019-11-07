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
            MemoryWritableData(FastByteArrayOutputStream())
    }

    override fun getOutputStream(): OutputStream =
        fastByteArrayOutputStream
}