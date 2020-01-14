package pl.beone.promena.transformer.applicationmodel.exception.data

/**
 * General exception class for data processing problems.
 */
open class DataOperationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)