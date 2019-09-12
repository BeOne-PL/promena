package pl.beone.promena.transformer.applicationmodel.exception.data

open class DataOperationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)