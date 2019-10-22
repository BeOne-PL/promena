package pl.beone.promena.core.applicationmodel.exception.transformation

class TransformationTerminationException(
    message: String,
    causeClass: Class<*>
) : TransformationException(message, causeClass)