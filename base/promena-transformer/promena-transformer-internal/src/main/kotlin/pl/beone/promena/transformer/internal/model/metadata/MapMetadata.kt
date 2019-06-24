package pl.beone.promena.transformer.internal.model.metadata

import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model._get
import pl.beone.promena.transformer.internal.model.get
import pl.beone.promena.transformer.internal.model.getList
import pl.beone.promena.transformer.internal.model.getListWithoutType

data class MapMetadata(private val metadata: Map<String, Any>) : Metadata {

    companion object {

        @JvmStatic
        fun empty(): Metadata = MapMetadata(emptyMap())
    }

    override fun get(key: String): Any =
            metadata._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
            metadata.get(key, clazz)

    override fun getMetadata(key: String): Metadata =
            metadata.get(key, Metadata::class.java)

    override fun getList(key: String): List<Any> =
            metadata.getListWithoutType(key)

    override fun <T> getList(key: String, clazz: Class<T>): List<T> =
            metadata.getList(key, clazz)

    override fun getAll(): Map<String, Any> =
            metadata
}