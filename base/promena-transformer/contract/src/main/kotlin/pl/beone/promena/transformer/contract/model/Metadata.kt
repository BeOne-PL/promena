package pl.beone.promena.transformer.contract.model

/**
 * Returns the metadata of [Data][pl.beone.promena.transformer.contract.model.data.Data]. Metadata provides additional information for a data.
 * It can contain any set of parameters including nested [Metadata] and [List].
 */
interface Metadata {

    /**
     * @throws NoSuchElementException if there is no [key]
     * @return the value of [key]
     */
    fun get(key: String): Any

    /**
     * Gets a value and tries to cast the value as [T].
     *
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] as [T]
     */
    fun <T> get(key: String, clazz: Class<T>): T

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return tge value of [key] or `null` if there is no [key]
     */
    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] as [T] or [default] if there is no [key]
     */
    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    /**
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion to [Metadata] isn't possible
     * @return the [Metadata] of [key]
     */
    fun getMetadata(key: String): Metadata

    /**
     * @throws TypeConversionException if conversion to [Metadata] isn't possible
     * @return the [Metadata] of [key] or `null` if there is no [key]
     */
    fun getMetadataOrNull(key: String): Metadata?

    /**
     * @throws TypeConversionException if conversion to [Metadata] isn't possible
     * @return the [Metadata] of [key] or [default] if there is no [key]
     */
    fun getMetadataOrDefault(key: String, default: Metadata): Metadata

    /**
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion to [List<Any>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<Any>][kotlin.collections.List]
     */
    fun getList(key: String): List<Any>

    /**
     * @throws TypeConversionException if conversion to [List<Any>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<Any>][kotlin.collections.List] or `null` if there is no [key]
     */
    fun getListOrNull(key: String): List<Any>?

    /**
     * @throws TypeConversionException if conversion to [List<Any>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<Any>][kotlin.collections.List] or [default] if there is no [key]
     */
    fun getListOrDefault(key: String, default: List<Any>): List<Any>

    /**
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion to [List<T>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<T>][kotlin.collections.List]
     */
    fun <T> getList(key: String, clazz: Class<T>): List<T>

    /**
     * @throws TypeConversionException if conversion to [List<T>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<T>][kotlin.collections.List] or `null` if there is no [key]
     */
    fun <T> getListOrNull(key: String, clazz: Class<T>): List<T>?

    /**
     * @throws TypeConversionException if conversion to [List<T>][kotlin.collections.List] isn't possible
     * @return the value of [key] as [List<T>][kotlin.collections.List] or [default] if there is no [key]
     */
    fun <T> getListOrDefault(key: String, clazz: Class<T>, default: List<T>): List<T>

    fun getAll(): Map<String, Any>
}