package pl.beone.promena.transformer.applicationmodel.exception.data

class DataDeleteException(
    message: String,
    cause: Throwable? = null
) : DataOperationException(message, cause)