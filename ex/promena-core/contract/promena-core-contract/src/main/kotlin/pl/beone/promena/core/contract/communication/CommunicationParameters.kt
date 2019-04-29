package pl.beone.promena.core.contract.communication

import java.net.URI

interface CommunicationParameters {

    @Throws(NoSuchElementException::class)
    fun get(key: String): Any

    @Throws(NoSuchElementException::class)
    fun <T> get(key: String, clazz: Class<T>): T

    @Throws(NoSuchElementException::class)
    fun getLocation(): URI

    fun getAll(): Map<String, Any>
}