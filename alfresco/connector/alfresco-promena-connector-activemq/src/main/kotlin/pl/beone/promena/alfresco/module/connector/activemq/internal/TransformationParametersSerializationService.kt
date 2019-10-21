package pl.beone.promena.alfresco.module.connector.activemq.internal

import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.core.contract.serialization.SerializationService

class TransformationParametersSerializationService(
    private val serializationService: SerializationService
) {

    fun serialize(transformationParameters: TransformationParameters): String =
        serializationService.serialize(transformationParameters)
            .joinToString(" ", transform = Byte::toString)

    fun deserialize(bytes: String): TransformationParameters =
        serializationService.deserialize(
            bytes.split(" ").map(String::toByte).toByteArray(),
            getClazz()
        )

    private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java
}