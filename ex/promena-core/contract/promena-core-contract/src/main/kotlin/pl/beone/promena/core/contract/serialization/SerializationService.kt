package pl.beone.promena.core.contract.serialization

import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException

interface SerializationService {

    @Throws(SerializationException::class)
    fun <T> serialize(element: T): ByteArray

    @Throws(DeserializationException::class)
    fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T
}