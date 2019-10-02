package pl.beone.promena.transformer.contract.model

interface Metadata {

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    @Throws(NoSuchElementException::class)
    fun getMetadata(key: String): Metadata

    fun getMetadataOrNull(key: String): Metadata?

    fun getMetadataOrDefault(key: String, default: Metadata): Metadata

    @Throws(NoSuchElementException::class)
    fun getList(key: String): List<Any>

    fun getListOrNull(key: String): List<Any>?

    fun getListOrDefault(key: String, default: List<Any>): List<Any>

    @Throws(NoSuchElementException::class)
    fun <T> getList(key: String, clazz: Class<T>): List<T>

    fun <T> getListOrNull(key: String, clazz: Class<T>): List<T>?

    fun <T> getListOrDefault(key: String, clazz: Class<T>, default: List<T>): List<T>

    fun getAll(): Map<String, Any>
}