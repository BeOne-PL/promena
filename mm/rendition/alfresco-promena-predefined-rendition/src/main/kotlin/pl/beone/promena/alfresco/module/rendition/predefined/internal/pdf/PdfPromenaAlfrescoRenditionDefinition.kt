package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation

class PdfPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "pdf"

    override fun getTransformation(nodeRef: NodeRef, mediaType: MediaType): Transformation =
        getTransformation(mediaType)
            ?: throw AlfrescoPromenaRenditionTransformationNotSupportedException(nodeRef, getRenditionName(), mediaType, APPLICATION_PDF)
}