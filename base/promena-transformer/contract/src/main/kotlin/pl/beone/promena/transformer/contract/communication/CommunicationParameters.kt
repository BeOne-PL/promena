package pl.beone.promena.transformer.contract.communication

/**
 * Returns parameters of communication.
 * It can contain any set of parameters, but each of them has to return [getId].
 */
interface CommunicationParameters {

    companion object {
        /**
         * Should be used as *id* key.
         */
        const val ID = "id"
    }

    /**
     * @throws NoSuchElementException if there is no [key]
     * @return the value of [key]
     */
    fun get(key: String): Any

    /**
     * Gets a value of [key] and tries to cast the value as [T].
     *
     * @throws NoSuchElementException if there is no [key]
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] as [T]
     */
    fun <T> get(key: String, clazz: Class<T>): T

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] or `null` if there is no [key]
     */
    fun <T> getOrNull(key: String, clazz: Class<T>): T?

    /**
     * @throws TypeConversionException if conversion isn't possible
     * @return the value of [key] or [default] if there is no [key]
     */
    fun <T> getOrDefault(key: String, clazz: Class<T>, default: T): T

    /**
     * @throws NoSuchElementException if there is no *id*
     * @return the value of *id*
     */
    fun getId(): String

    fun getAll(): Map<String, Any>
}