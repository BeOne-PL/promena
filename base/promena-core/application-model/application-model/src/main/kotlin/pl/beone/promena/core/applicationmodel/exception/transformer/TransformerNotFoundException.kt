package pl.beone.promena.core.applicationmodel.exception.transformer

/**
 * Signals that there is no transformer that meets requirements.
 */
class TransformerNotFoundException(
    message: String,
    cause: Throwable? = null
) : TransformerException(message, cause)