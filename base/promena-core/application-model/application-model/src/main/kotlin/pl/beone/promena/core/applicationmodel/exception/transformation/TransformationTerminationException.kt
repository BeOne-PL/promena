package pl.beone.promena.core.applicationmodel.exception.transformation

import pl.beone.promena.transformer.contract.transformation.Transformation

class TransformationTerminationException(
    transformation: Transformation,
    message: String,
    causeClass: Class<*>? = null,
    cause: Throwable? = null
) : TransformationException(transformation, message, causeClass, cause)