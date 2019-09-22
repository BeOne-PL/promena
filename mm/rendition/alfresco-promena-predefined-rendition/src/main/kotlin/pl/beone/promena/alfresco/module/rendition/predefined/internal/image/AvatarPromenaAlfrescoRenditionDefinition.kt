package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_PNG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

class AvatarPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "avatar"

    override fun getTransformation(nodeRef: NodeRef, mediaType: MediaType): Transformation =
        getTransformation(
            mediaType,
            imageMagickConverterTransformation(IMAGE_PNG, imageMagickConverterParameters(width = 64, height = 64, allowEnlargement = false))
        ) ?: throw AlfrescoPromenaRenditionTransformationNotSupportedException.unsupportedMediaType(nodeRef, getRenditionName(), mediaType, IMAGE_PNG)

    override fun getPlaceHolderResourcePath(): String? =
        "alfresco/thumbnail/thumbnail_placeholder_avatar.png"
}