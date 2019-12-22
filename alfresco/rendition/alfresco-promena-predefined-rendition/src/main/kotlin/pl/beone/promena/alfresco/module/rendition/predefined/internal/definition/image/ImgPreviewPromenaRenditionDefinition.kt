package pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.image

import pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception.PromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_JPEG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

object ImgPreviewPromenaRenditionDefinition : PromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "imgpreview"

    override fun getTargetMediaType(): MediaType =
        IMAGE_JPEG

    override fun getTransformation(mediaType: MediaType): Transformation =
        try {
            determineTransformation(
                mediaType,
                getTargetMediaType(),
                imageMagickConverterTransformation(
                    getTargetMediaType(),
                    imageMagickConverterParameters(width = 960, height = 960, ignoreAspectRatio = false, allowEnlargement = false)
                )
            )
        } catch (e: TransformationNotSupportedException) {
            throw PromenaRenditionTransformationNotSupportedException.unsupportedMediaType(
                getRenditionName(), mediaType,
                getTargetMediaType()
            )
        }

    override fun getPlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_256.png"

    override fun getMimeAwarePlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_256{0}.png"
}