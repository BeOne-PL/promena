package pl.beone.promena.core.applicationmodel.exception.transformer

open class TransformerException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)