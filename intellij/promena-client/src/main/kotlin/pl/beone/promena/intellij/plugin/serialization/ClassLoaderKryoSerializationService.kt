package pl.beone.promena.intellij.plugin.serialization

import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.internal.serialization.KryoSerializationService

class ClassLoaderKryoSerializationService(classLoader: ClassLoader) : SerializationService {

    private val kryoSerializationService = classLoader.loadClass(KryoSerializationService::class.java.canonicalName)
        .getDeclaredConstructor().newInstance()

    override fun <T> serialize(element: T): ByteArray =
        KryoSerializationService::class.java.getMethod("serialize", Any::class.java)
            .invoke(kryoSerializationService, element) as ByteArray

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
        KryoSerializationService::class.java.getMethod("deserialize", ByteArray::class.java, Class::class.java)
            .invoke(kryoSerializationService, bytes, clazz) as T
}