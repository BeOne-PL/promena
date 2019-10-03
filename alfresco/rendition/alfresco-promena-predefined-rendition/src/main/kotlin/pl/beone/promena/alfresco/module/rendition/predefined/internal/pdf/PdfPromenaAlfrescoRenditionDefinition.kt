package pl.beone.promena.alfresco.module.rendition.predefined.internal.pdf

import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation

object PdfPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "pdf"

    override fun getTargetMediaType(): MediaType =
        APPLICATION_PDF

    override fun getTransformation(mediaType: MediaType): Transformation =
        try {
            determineTransformation(mediaType, getTargetMediaType())
        } catch (e: TransformationNotSupportedException) {
            throw AlfrescoPromenaRenditionTransformationNotSupportedException.unsupportedMediaType(getRenditionName(), mediaType, getTargetMediaType())
        }
}