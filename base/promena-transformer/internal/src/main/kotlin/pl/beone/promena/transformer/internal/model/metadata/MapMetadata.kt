package pl.beone.promena.transformer.internal.model.metadata

import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.internal.model.extensions.*

/**
 * The implementation based on [Map].
 *
 * @see MapMetadataDsl
 * @see MapMetadataBuilder
 */
data class MapMetadata internal constructor(
    private val metadata: Map<String, Any>
) : Metadata {

    companion object {
        @JvmStatic
        fun empty(): MapMetadata =
            MapMetadata(emptyMap())

        @JvmStatic
        fun of(metadata: Map<String, Any>): MapMetadata =
            MapMetadata(metadata)
    }

    override fun get(key: String): Any =
        metadata._get(key)

    override fun <T> get(key: String, clazz: Class<T>): T =
        metadata.get(key, clazz)

    override fun <T> getOrNull(key: String, clazz: Class<T>): T? =
        metadata.getOrNull(key, clazz)

    override fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T =
        metadata.getOrDefault(key, clazz, default)

    override fun getMetadata(key: String): Metadata =
        metadata.get(key, Metadata::class.java)

    override fun getMetadataOrNull(key: String): Metadata? =
        metadata.getOrNull(key, Metadata::class.java)

    override fun getMetadataOrDefault(key: String, default: Metadata): Metadata =
        metadata.getOrDefault(key, Metadata::class.java, default)

    override fun getList(key: String): List<Any> =
        metadata.getListWithoutType(key)

    override fun getListOrNull(key: String): List<Any>? =
        metadata.getListWithoutTypeOrNull(key)

    override fun getListOrDefault(key: String, default: List<Any>): List<Any> =
        metadata.getListWithoutTypeOrDefault(key, default)

    override fun <T> getList(key: String, clazz: Class<T>): List<T> =
        metadata.getList(key, clazz)

    override fun <T> getListOrNull(key: String, clazz: Class<T>): List<T>? =
        metadata.getListOrNull(key, clazz)

    override fun <T> getListOrDefault(key: String, clazz: Class<T>, default: List<T>): List<T> =
        metadata.getListOrDefault(key, clazz, default)

    override fun getAll(): Map<String, Any> =
        metadata
}