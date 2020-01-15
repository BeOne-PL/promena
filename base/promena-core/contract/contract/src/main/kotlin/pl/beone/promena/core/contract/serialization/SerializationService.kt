package pl.beone.promena.core.contract.serialization

import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException

interface SerializationService {

    /**
     * @throws SerializationException if an error has occurred during serialization
     */
    fun <T> serialize(element: T): ByteArray

    /**
     * @throws DeserializationException if an error has occurred during deserialization
     */
    fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T
}