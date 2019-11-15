package pl.beone.promena.transformer.applicationmodel.exception.transformer

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class TransformationNotSupportedException internal constructor(
    reason: String
) : IllegalArgumentException(reason) {

    companion object {
        @JvmStatic
        fun unsupportedMediaType(mediaType: MediaType, targetMediaType: MediaType): TransformationNotSupportedException =
            TransformationNotSupportedException("Transformation (${mediaType.createDescription()}) -> (${targetMediaType.createDescription()}) isn't supported")

        @JvmStatic
        fun mandatoryParameter(name: String): TransformationNotSupportedException =
            TransformationNotSupportedException("Parameter <$name> is mandatory")

        @JvmStatic
        fun unsupportedParameterType(name: String, clazz: Class<*>): TransformationNotSupportedException =
            TransformationNotSupportedException("Parameter <$name> isn't type of <${clazz.canonicalName}>")

        @JvmStatic
        fun <T> unsupportedParameterValue(name: String, value: T, message: String? = null): TransformationNotSupportedException =
            TransformationNotSupportedException("Parameter <$name> has invalid value <$value>${if (message != null) ": $message" else ""}")

        @JvmStatic
        fun custom(reason: String): TransformationNotSupportedException =
            TransformationNotSupportedException(reason)

        private fun MediaType.createDescription(): String =
            "${mimeType}, ${charset.name()}"
    }
}