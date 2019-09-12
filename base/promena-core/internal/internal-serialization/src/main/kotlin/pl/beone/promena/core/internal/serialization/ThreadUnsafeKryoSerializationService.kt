package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import pl.beone.promena.core.internal.serialization.util.createKryo

class ThreadUnsafeKryoSerializationService(
    classLoader: ClassLoader? = null,
    bufferSize: Int = 100 * 1024 * 1024 // 100MB
) : AbstractKryoSerializationService(bufferSize) {

    private val kryo = createKryo(classLoader)

    override fun getKryo(): Kryo =
        kryo
}