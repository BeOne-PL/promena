package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_JPEG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

class ImgPreviewPromenaAlfrescoRenditionDefinition : PromenaAlfrescoRenditionDefinition {

    override fun getRenditionName(): String =
        "imgpreview"

    override fun getTransformation(): Transformation =
        imageMagickConverterTransformation(
            IMAGE_JPEG,
            imageMagickConverterParameters(width = 960, height = 960, allowEnlargement = false)
        )
}