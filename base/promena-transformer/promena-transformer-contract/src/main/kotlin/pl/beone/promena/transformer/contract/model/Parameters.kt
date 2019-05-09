package pl.beone.promena.transformer.contract.model

interface Parameters {

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    @Throws(NoSuchElementException::class)
    fun getTimeout(): Long

    @Throws(NoSuchElementException::class)
    fun getParameters(key: String): Parameters

    @Throws(NoSuchElementException::class)
    fun getList(key: String): List<Any>

    @Throws(NoSuchElementException::class)
    fun <T> getList(key: String, clazz: Class<T>): List<T>

    fun getAll(): Map<String, Any>
}