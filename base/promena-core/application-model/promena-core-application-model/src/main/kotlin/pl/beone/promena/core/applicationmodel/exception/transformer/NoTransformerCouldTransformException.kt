package pl.beone.promena.core.applicationmodel.exception.transformer

class NoTransformerCouldTransformException(
    message: String,
    cause: Throwable? = null
) : TransformerException(message, cause)