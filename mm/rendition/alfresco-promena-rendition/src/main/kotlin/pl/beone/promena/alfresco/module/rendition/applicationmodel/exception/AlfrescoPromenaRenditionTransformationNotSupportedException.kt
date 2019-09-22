package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class AlfrescoPromenaRenditionTransformationNotSupportedException internal constructor(
    reason: String
) : IllegalArgumentException(reason) {

    companion object {
        @JvmStatic
        fun unsupportedMediaType(
            renditionName: String,
            mediaType: MediaType,
            targetMediaType: MediaType
        ): AlfrescoPromenaRenditionTransformationNotSupportedException =
            AlfrescoPromenaRenditionTransformationNotSupportedException("Rendition <$renditionName> transformation ${mediaType.createDescription()} -> ${targetMediaType.createDescription()} isn't supported")

        @JvmStatic
        fun custom(reason: String): AlfrescoPromenaRenditionTransformationNotSupportedException =
            AlfrescoPromenaRenditionTransformationNotSupportedException(reason)

        private fun MediaType.createDescription(): String =
            "(${mimeType}, ${charset.name()})"
    }
}