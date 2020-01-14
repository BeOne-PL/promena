package pl.beone.promena.transformer.applicationmodel.exception.data

/**
 * Signals that an error has occurred while deleting a data.
 */
class DataDeleteException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)