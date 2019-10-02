package pl.beone.promena.transformer.contract.model

import java.time.Duration

interface Parameters {

    companion object {
        const val TIMEOUT = "timeout"
    }

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    fun getOrNull(key: String): Any?

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    @Throws(NoSuchElementException::class)
    fun getTimeout(): Duration

    fun getTimeoutOrNull(): Duration?

    fun getTimeoutOrDefault(default: Duration): Duration

    @Throws(NoSuchElementException::class)
    fun getParameters(key: String): Parameters

    fun getParametersOrNull(key: String): Parameters?

    fun getParametersOrDefault(key: String, default: Parameters): Parameters

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