package pl.beone.promena.alfresco.module.core.applicationmodel.exception

class PostTransformationExecutorValidationException(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)