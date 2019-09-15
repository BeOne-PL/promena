package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_PNG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

class AvatarPromenaAlfrescoRenditionDefinition : PromenaAlfrescoRenditionDefinition {

    override fun getRenditionName(): String =
        "avatar"

    override fun getTransformation(): Transformation =
        imageMagickConverterTransformation(
            IMAGE_PNG,
            imageMagickConverterParameters(width = 64, height = 64, allowEnlargement = false)
        )
}