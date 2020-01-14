package pl.beone.promena.transformer.contract.model

import java.time.Duration

/**
 * Returns the parameters of [Transformation][pl.beone.promena.transformer.contract.transformation.Transformation].
 * It can contain any set of parameters including nested [Parameters] and [List].
 */
interface Parameters {

    companion object {
        /**
         * Should be used as *timeout* key
         */
        const val TIMEOUT = "timeout"
    }

    /**
     * @throws NoSuchElementException if there is no [key]
     * @return the value of [key]
     */
    fun get(key: String): Any

    /**
     * @throws NoSuchElementException if there is no [key]
     * @return the value of [key] or `null` if there is no [key]
     */
    fun getOrNull(key: String): Any?

    /**
     * Gets a value and tries to cast the value as [T].
     *
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion isn't possible
     * @return a value of [key] and tries to cast as [T] type
     */
    fun <T> get(key: String, clazz: Class<T>): T

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] as [T] or `null` if there is no [key]
     */
    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] as [T] or [default] if there is no [key]
     */
    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    /**
     * @throws NoSuchElementException if there is no *timeout*
     * @return the duration of *timeout*
     */
    fun getTimeout(): Duration

    /**
     * @throws NoSuchElementException if there is no *timeout*
     * @return the duration of *timeout* or `null` if there is no *timeout*
     */
    fun getTimeoutOrNull(): Duration?

    /**
     * @throws TypeConversionException if conversion to [Duration] isn't possible
     * @return the duration of *timeout* or [default] if there is no *timeout*
     */
    fun getTimeoutOrDefault(default: Duration): Duration

    /**
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion to [Parameters] isn't possible
     * @return the value of [key] as [Parameters]
     */
    fun getParameters(key: String): Parameters

    /**
     * @throws TypeConversionException if conversion to [Parameters] isn't possible
     * @return the value of [key] as [Parameters] or `null` if there is no [key]
     */
    fun getParametersOrNull(key: String): Parameters?

    /**
     * @throws TypeConversionException if conversion to [Parameters] isn't possible
     * @return the value of [key] as [Parameters] or [default] if there is no [key]
     */
    fun getParametersOrDefault(key: String, default: Parameters): Parameters

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