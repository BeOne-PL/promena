package pl.beone.promena.core.applicationmodel.exception.communication.external.manager

class ExternalCommunicationManagerValidationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)