package pl.beone.promena.core.applicationmodel.exception.transformer

class TransformersCouldNotTransformException(
    message: String,
    cause: Throwable? = null
) : TransformerException(message, cause)