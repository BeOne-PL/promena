package pl.beone.promena.core.applicationmodel.exception.transformer

class TransformerNotFoundException(
    message: String,
    cause: Throwable? = null
) : TransformerException(message, cause)