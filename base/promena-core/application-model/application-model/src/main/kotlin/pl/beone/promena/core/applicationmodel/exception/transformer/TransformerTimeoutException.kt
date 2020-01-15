package pl.beone.promena.core.applicationmodel.exception.transformer

/**
 * Signals that a timeout has been reached by a transformer.
 */
class TransformerTimeoutException(
    message: String,
    cause: Throwable? = null
) : TransformerException(message, cause)