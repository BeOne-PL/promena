package pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.pdf

import pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception.PromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.LibreOfficeConverterSupport
import pl.beone.promena.transformer.converter.libreoffice.applicationmodel.libreOfficeConverterTransformation

class PdfPromenaRenditionDefinition() : PromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "pdf"

    override fun getTargetMediaType(): MediaType =
        APPLICATION_PDF

    override fun getTransformation(mediaType: MediaType): Transformation {
        throwIfMimeTypePrimaryTypeIsImage(mediaType)

        return try {
            LibreOfficeConverterSupport.MediaTypeSupport.isSupported(mediaType, getTargetMediaType())

            libreOfficeConverterTransformation()
        } catch (e: TransformationNotSupportedException) {
            throw PromenaRenditionTransformationNotSupportedException.unsupportedMediaType(getRenditionName(), mediaType, getTargetMediaType())
        }
    }

    // imgpreview rendition will be prefer by default
    private fun throwIfMimeTypePrimaryTypeIsImage(mediaType: MediaType) {
        if (mediaType.mimeType.startsWith("image")) {
            throw PromenaRenditionTransformationNotSupportedException.unsupportedMediaType(getRenditionName(), mediaType, getTargetMediaType())
        }
    }
}