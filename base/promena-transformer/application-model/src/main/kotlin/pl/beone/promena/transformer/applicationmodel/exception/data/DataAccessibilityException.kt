package pl.beone.promena.transformer.applicationmodel.exception.data

class DataAccessibilityException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)