package pl.beone.promena.transformer.applicationmodel.exception.data

/**
 * Signals that a data isn't accessible.
 */
class DataAccessibilityException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)