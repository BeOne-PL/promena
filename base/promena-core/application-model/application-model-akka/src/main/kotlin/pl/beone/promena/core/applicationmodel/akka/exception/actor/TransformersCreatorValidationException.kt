package pl.beone.promena.core.applicationmodel.akka.exception.actor

class TransformersCreatorValidationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)