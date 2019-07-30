package pl.beone.promena.core.applicationmodel.exception.communication

class CommunicationParametersValidationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)