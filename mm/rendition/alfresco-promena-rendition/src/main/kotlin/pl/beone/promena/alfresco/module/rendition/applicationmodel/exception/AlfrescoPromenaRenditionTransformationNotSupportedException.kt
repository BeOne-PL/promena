package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class AlfrescoPromenaRenditionTransformationNotSupportedException(
    reason: String
) : IllegalArgumentException(reason) {

    constructor(
        nodeRef: NodeRef,
        renditionName: String,
        mediaType: MediaType,
        targetMediaType: MediaType
    ) : this("Rendition <$renditionName> transformation ${mediaType.createDescription()} -> ${targetMediaType.createDescription()} for <$nodeRef> isn't supported")
}

private fun MediaType.createDescription(): String =
    "(${mimeType}, ${charset.name()})"