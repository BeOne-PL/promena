package pl.beone.promena.alfresco.module.rendition.extension

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import java.nio.charset.Charset

fun ContentService.getMediaType(nodeRef: NodeRef, propertyQName: QName = PROP_CONTENT): MediaType {
    val contentReader = this.getReader(nodeRef, propertyQName)
    return mediaType(contentReader.mimetype, Charset.forName(contentReader.encoding))
}