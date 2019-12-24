package pl.beone.promena.core.applicationmodel.exception.transformation

class TransformationTerminationException(
    message: String,
    causeClass: String? = null
) : TransformationException(message, causeClass)