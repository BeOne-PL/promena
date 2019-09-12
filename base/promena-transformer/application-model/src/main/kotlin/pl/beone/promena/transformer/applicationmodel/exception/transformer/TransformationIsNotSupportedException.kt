package pl.beone.promena.transformer.applicationmodel.exception.transformer

class TransformationNotSupportedException(
    reason: String
) : IllegalArgumentException(reason)