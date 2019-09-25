package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import pl.beone.promena.core.internal.serialization.util.createKryo

class ClassLoaderKryoSerializationService(
    classLoader: ClassLoader? = null,
    private val lockObject: Any? = null,
    bufferSize: Int = 100 * 1024 * 1024 // 100MB
) : AbstractKryoSerializationService(bufferSize) {

    private val kryo = createKryo(classLoader)

    override fun getKryo(): Kryo =
        kryo

    override fun <T> serialize(element: T): ByteArray =
        if (lockObject != null) {
            synchronized(lockObject) {
                super.serialize(element)
            }
        } else {
            super.serialize(element)
        }

    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
        if (lockObject != null) {
            synchronized(lockObject) {
                super.deserialize(bytes, clazz)
            }
        } else {
            super.deserialize(bytes, clazz)
        }
}