package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_JPEG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

class ImgPreviewPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "imgpreview"

    override fun getTargetMediaType(): MediaType =
        IMAGE_JPEG

    override fun getTransformation(mediaType: MediaType): Transformation =
        determineTransformation(
            mediaType,
            imageMagickConverterTransformation(getTargetMediaType(), imageMagickConverterParameters(width = 960, height = 960, allowEnlargement = false))
        ) ?: throw AlfrescoPromenaRenditionTransformationNotSupportedException.unsupportedMediaType(getRenditionName(), mediaType, getTargetMediaType())

    override fun getPlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_256.png"

    override fun getMimeAwarePlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_256{0}.png"
}