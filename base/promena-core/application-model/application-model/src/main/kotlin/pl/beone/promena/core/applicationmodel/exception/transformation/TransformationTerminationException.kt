package pl.beone.promena.core.applicationmodel.exception.transformation

/**
 * Signals that a transformation execution has been abruptly terminated.
 */
class TransformationTerminationException(
    message: String,
    causeClass: String? = null
) : TransformationException(message, causeClass)