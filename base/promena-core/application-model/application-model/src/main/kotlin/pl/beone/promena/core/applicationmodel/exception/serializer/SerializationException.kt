package pl.beone.promena.core.applicationmodel.exception.serializer

/**
 * Signals that an error has occurred during serialization of a data.
 */
class SerializationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)