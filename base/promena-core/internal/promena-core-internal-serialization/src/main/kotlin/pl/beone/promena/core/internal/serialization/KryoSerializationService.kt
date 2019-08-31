package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import pl.beone.promena.core.internal.serialization.util.KryoThreadLocal

class KryoSerializationService(
    bufferSize: Int = 100 * 1024 * 1024 // 100MB
) : AbstractKryoSerializationService(bufferSize) {

    override fun getKryo(): Kryo =
        KryoThreadLocal.instance.get()
}