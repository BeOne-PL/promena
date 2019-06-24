package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model._get
import pl.beone.promena.transformer.internal.model.get
import pl.beone.promena.transformer.internal.model.getList
import pl.beone.promena.transformer.internal.model.getListWithoutType

data class MapParameters(private val parameters: Map<String, Any>) : Parameters {

    companion object {

        @JvmStatic
        fun empty(): Parameters = MapParameters(emptyMap())
    }

    override fun get(key: String): Any =
            parameters._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
            parameters.get(key, clazz)

    override fun getTimeout(): Long =
            parameters.get("timeout", Long::class.java)

    override fun getParameters(key: String): Parameters =
            parameters.get(key, Parameters::class.java)

    override fun getList(key: String): List<Any> =
            parameters.getListWithoutType(key)

    override fun <T> getList(key: String, clazz: Class<T>): List<T> =
            parameters.getList(key, clazz)

    override fun getAll(): Map<String, Any> =
            parameters


}