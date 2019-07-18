package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.model._get
import pl.beone.promena.transformer.internal.model.get

data class MapCommunicationParameters internal constructor(private val parameters: Map<String, Any>) : CommunicationParameters {

    companion object {

        @JvmStatic
        fun empty(): MapCommunicationParameters = MapCommunicationParameters(emptyMap())

        @JvmStatic
        fun of(id: String, parameters: Map<String, Any>? = null): MapCommunicationParameters =
                MapCommunicationParameters(mapOf("id" to id) + (parameters ?: emptyMap()))

        @JvmStatic
        fun builder(): MapCommunicationParametersBuilder =
                MapCommunicationParametersBuilder(HashMap())
    }

    override fun get(key: String): Any =
            parameters._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
            parameters.get(key, clazz)

    override fun getId(): String =
            parameters.get("id", String::class.java)

    override fun getAll(): Map<String, Any> =
            parameters
}