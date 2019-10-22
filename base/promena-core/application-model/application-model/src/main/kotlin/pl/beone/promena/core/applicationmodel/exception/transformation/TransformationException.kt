package pl.beone.promena.core.applicationmodel.exception.transformation

open class TransformationException(
    message: String,
    val causeClass: Class<*>
) : RuntimeException(message)