package pl.beone.promena.core.applicationmodel.exception.serializer

/**
 * Signals that an error has occurred during deserialization of a data.
 */
class DeserializationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)