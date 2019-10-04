package pl.beone.promena.core.applicationmodel.exception.transformation

import pl.beone.promena.transformer.contract.transformation.Transformation

open class TransformationException(
    val transformation: Transformation,
    message: String,
    val causeClass: Class<*>? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)