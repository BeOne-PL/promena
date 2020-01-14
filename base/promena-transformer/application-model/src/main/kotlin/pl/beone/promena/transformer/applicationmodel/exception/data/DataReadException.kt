package pl.beone.promena.transformer.applicationmodel.exception.data

/**
 * Signals that an error has occurred while reading a data.
 */
class DataReadException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)