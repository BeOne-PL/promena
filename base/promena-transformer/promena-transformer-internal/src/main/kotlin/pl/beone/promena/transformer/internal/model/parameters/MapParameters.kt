package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.Parameters.Companion.Timeout
import pl.beone.promena.transformer.internal.model._get
import pl.beone.promena.transformer.internal.model.get
import pl.beone.promena.transformer.internal.model.getList
import pl.beone.promena.transformer.internal.model.getListWithoutType
import java.time.Duration

data class MapParameters internal constructor(private val parameters: Map<String, Any>) : Parameters {

    companion object {

        @JvmStatic
        fun empty(): MapParameters =
                MapParameters(emptyMap())

        @JvmStatic
        fun of(parameters: Map<String, Any>, timeout: Duration? = null): MapParameters =
                MapParameters(parameters +
                              if (timeout != null) mapOf(Timeout to timeout) else emptyMap())

        @JvmStatic
        fun builder(): MapParametersBuilder =
                MapParametersBuilder(HashMap())
    }

    override fun get(key: String): Any =
            parameters._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
            parameters.get(key, clazz)

    override fun getTimeout(): Duration =
            parameters.get(Timeout, Duration::class.java)

    override fun getParameters(key: String): Parameters =
            parameters.get(key, Parameters::class.java)

    override fun getList(key: String): List<Any> =
            parameters.getListWithoutType(key)

    override fun <T> getList(key: String, clazz: Class<T>): List<T> =
            parameters.getList(key, clazz)

    override fun getAll(): Map<String, Any> =
            parameters


}