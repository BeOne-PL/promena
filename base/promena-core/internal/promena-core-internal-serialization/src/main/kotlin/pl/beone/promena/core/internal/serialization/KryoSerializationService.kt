package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException
import pl.beone.promena.core.contract.serialization.SerializationService

// default = 100MB
class KryoSerializationService(private val bufferSize: Int = 100 * 1024 * 1024) : SerializationService {

    private val kryo = createKryo()

    override fun <T> serialize(element: T): ByteArray =
        try {
            with(ByteBufferOutput(bufferSize)) {
                kryo.writeClassAndObject(this, element)
                return this.toBytes()
            }
        } catch (e: Exception) {
            throw SerializationException("Couldn't serialize", e)
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
        try {
            kryo.readClassAndObject(ByteBufferInput(bytes)) as T
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize", e)
        }
}