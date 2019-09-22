package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class AlfrescoPromenaRenditionTransformationNotSupportedException internal constructor(
    reason: String
) : IllegalArgumentException(reason) {

    companion object {
        @JvmStatic
        fun unsupportedMediaType(
            nodeRef: NodeRef,
            renditionName: String,
            mediaType: MediaType,
            targetMediaType: MediaType
        ): AlfrescoPromenaRenditionTransformationNotSupportedException =
            AlfrescoPromenaRenditionTransformationNotSupportedException("Rendition <$renditionName> transformation ${mediaType.createDescription()} -> ${targetMediaType.createDescription()} for <$nodeRef> isn't supported")

        @JvmStatic
        fun custom(reason: String): AlfrescoPromenaRenditionTransformationNotSupportedException =
            AlfrescoPromenaRenditionTransformationNotSupportedException(reason)


        private fun MediaType.createDescription(): String =
            "(${mimeType}, ${charset.name()})"
    }
}