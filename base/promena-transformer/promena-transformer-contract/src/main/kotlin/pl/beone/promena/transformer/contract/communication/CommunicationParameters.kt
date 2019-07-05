package pl.beone.promena.transformer.contract.communication

interface CommunicationParameters {

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    @Throws(NoSuchElementException::class)
    fun getId(): String

    fun getAll(): Map<String, Any>
}