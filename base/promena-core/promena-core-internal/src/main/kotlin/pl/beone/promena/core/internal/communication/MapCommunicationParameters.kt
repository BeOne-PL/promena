package pl.beone.promena.core.internal.communication

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.model._get
import pl.beone.promena.transformer.internal.model.get
import java.net.URI

data class MapCommunicationParameters(private val parameters: Map<String, Any>) : CommunicationParameters {

    companion object {
        fun empty(): CommunicationParameters = MapCommunicationParameters(
                emptyMap())
    }

    override fun get(key: String): Any =
            parameters._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
            parameters.get(key, clazz)

    override fun getLocation(): URI =
            parameters.get("location", URI::class.java)

    override fun getAll(): Map<String, Any> =
            parameters
}