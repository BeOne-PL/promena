package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.model.extensions._get
import pl.beone.promena.transformer.internal.model.extensions.get
import pl.beone.promena.transformer.internal.model.extensions.getOrDefault
import pl.beone.promena.transformer.internal.model.extensions.getOrNull

/**
 * The implementation based on [Map].
 *
 * @see MapCommunicationParametersDsl
 * @see MapCommunicationParametersBuilder
 */
data class MapCommunicationParameters internal constructor(
    private val parameters: Map<String, Any>
) : CommunicationParameters {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun of(id: String, parameters: Map<String, Any> = emptyMap()): MapCommunicationParameters =
            MapCommunicationParameters(parameters + mapOf(CommunicationParameters.ID to id))
    }

    override fun get(key: String): Any =
        parameters._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
        parameters.get(key, clazz)

    override fun <T> getOrNull(key: String, clazz: Class<T>): T? =
        parameters.getOrNull(key, clazz)

    override fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T =
        parameters.getOrDefault(key, clazz, default)

    override fun getId(): String =
        parameters.get(CommunicationParameters.ID, String::class.java)

    override fun getAll(): Map<String, Any> =
        parameters
}