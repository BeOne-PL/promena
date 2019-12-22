package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class PromenaRenditionTransformationNotSupportedException internal constructor(
    reason: String
) : IllegalArgumentException(reason) {

    companion object {
        @JvmStatic
        fun unsupportedMediaType(
            renditionName: String,
            mediaType: MediaType,
            targetMediaType: MediaType
        ): PromenaRenditionTransformationNotSupportedException =
            PromenaRenditionTransformationNotSupportedException("Rendition <$renditionName> transformation ${mediaType.createDescription()} -> ${targetMediaType.createDescription()} isn't supported")

        @JvmStatic
        fun custom(reason: String): PromenaRenditionTransformationNotSupportedException =
            PromenaRenditionTransformationNotSupportedException(reason)

        private fun MediaType.createDescription(): String =
            "(${mimeType}, ${charset.name()})"
    }
}