package pl.beone.promena.core.applicationmodel.exception.transformer

/**
 * Signals that an error has occurred during a transformation execution by a transformer.
 */
open class TransformerException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)