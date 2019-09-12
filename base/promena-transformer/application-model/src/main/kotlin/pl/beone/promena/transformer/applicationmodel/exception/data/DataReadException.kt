package pl.beone.promena.transformer.applicationmodel.exception.data

class DataReadException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)