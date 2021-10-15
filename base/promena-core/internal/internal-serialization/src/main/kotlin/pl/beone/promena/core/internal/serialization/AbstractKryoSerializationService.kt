package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import com.esotericsoftware.kryo.io.Output
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException
import pl.beone.promena.core.contract.serialization.SerializationService
import java.io.ByteArrayOutputStream

abstract class AbstractKryoSerializationService(
    private val bufferSize: Int
) : SerializationService {

    override fun <T> serialize(element: T): ByteArray =
        try {
            val os = ByteArrayOutputStream()
            with(Output(os)) {
                getKryo().writeClassAndObject(this, element)
                os.writeBytes(this.toBytes())

                if (os.size() > bufferSize) {
                    throw SerializationException("Serialization failed. Insufficient buffer size.")
                }
                os.toByteArray()
            }
        } catch (e: Exception) {
            throw SerializationException("Couldn't serialize", e)
        }

    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
        try {
            @Suppress("UNCHECKED_CAST")
            getKryo().readClassAndObject(ByteBufferInput(bytes)) as T
        } catch  (e: Exception) {
            throw DeserializationException("Couldn't deserialize", e)
        }

    protected abstract fun getKryo(): Kryo
}