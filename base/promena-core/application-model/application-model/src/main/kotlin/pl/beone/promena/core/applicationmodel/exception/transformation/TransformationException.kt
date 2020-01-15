package pl.beone.promena.core.applicationmodel.exception.transformation

/**
 * Signals that an error has occurred during a transformation execution.
 */
open class TransformationException(
    message: String,
    val causeClass: String? = null
) : RuntimeException(message)