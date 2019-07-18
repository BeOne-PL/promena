package pl.beone.promena.transformer.contract.communication

interface CommunicationParameters {

    companion object {
        const val Id = "id"
    }

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    @Throws(NoSuchElementException::class)
    fun getId(): String

    fun getAll(): Map<String, Any>
}