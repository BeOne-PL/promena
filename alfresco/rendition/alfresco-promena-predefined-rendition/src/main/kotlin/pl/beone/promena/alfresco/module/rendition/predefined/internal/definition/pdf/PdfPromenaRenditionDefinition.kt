package pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.pdf

import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.Transformation

class PdfPromenaRenditionDefinition(
    private val applyForImages: Boolean
) : PromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "pdf"

    override fun getTargetMediaType(): MediaType =
        APPLICATION_PDF

    override fun getTransformation(mediaType: MediaType): Transformation {
        if (!applyForImages) {
            throwIfMimeTypePrimaryTypeIsImage(mediaType)
        }

        return try {
            determineTransformation(mediaType, getTargetMediaType())
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