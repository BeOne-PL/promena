package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_PNG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

object Avatar32PromenaRenditionDefinition : PromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "avatar32"

    override fun getTargetMediaType(): MediaType =
        IMAGE_PNG

    override fun getTransformation(mediaType: MediaType): Transformation =
        try {
            determineTransformation(
                mediaType,
                getTargetMediaType(),
                imageMagickConverterTransformation(
                    getTargetMediaType(),
                    imageMagickConverterParameters(width = 32, height = 32, allowEnlargement = false)
                )
            )
        } catch (e: TransformationNotSupportedException) {
            throw PromenaRenditionTransformationNotSupportedException.unsupportedMediaType(getRenditionName(), mediaType, getTargetMediaType())
        }

    override fun getPlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_avatar32.png"
}