package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.Parameters.Companion.TIMEOUT
import pl.beone.promena.transformer.internal.model.extensions.*
import java.time.Duration

data class MapParameters internal constructor(
    private val parameters: Map<String, Any>
) : Parameters {

    companion object {
        @JvmStatic
        fun empty(): MapParameters =
            MapParameters(emptyMap())

        @JvmStatic
        @JvmOverloads
        fun of(parameters: Map<String, Any>, timeout: Duration? = null): MapParameters =
            MapParameters(
                parameters +
                        if (timeout != null) mapOf(TIMEOUT to timeout) else emptyMap()
            )
    }

    override fun get(key: String): Any =
        parameters._get(key)

    override fun getOrNull(key: String): Any? =
        parameters.getOrNull(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
        parameters.get(key, clazz)

    override fun <T> getOrNull(key: String, clazz: Class<T>): T? =
        parameters.getOrNull(key, clazz)

    override fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T =
        parameters.getOrDefault(key, clazz, default)

    override fun getTimeout(): Duration =
        parameters.get(TIMEOUT, Duration::class.java)

    override fun getTimeoutOrDefault(default: Duration): Duration =
        parameters.getOrDefault(TIMEOUT, Duration::class.java, default)

    override fun getParameters(key: String): Parameters =
        parameters.get(key, Parameters::class.java)

    override fun getList(key: String): List<Any> =
        parameters.getListWithoutType(key)

    override fun <T> getList(key: String, clazz: Class<T>): List<T> =
        parameters.getList(key, clazz)

    override fun getAll(): Map<String, Any> =
        parameters
}