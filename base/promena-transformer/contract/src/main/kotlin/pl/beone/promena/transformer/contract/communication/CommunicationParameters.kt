package pl.beone.promena.transformer.contract.communication

interface CommunicationParameters {

    companion object {
        const val ID = "id"
    }

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    @Throws(NoSuchElementException::class)
    fun getId(): String

    fun getAll(): Map<String, Any>
}