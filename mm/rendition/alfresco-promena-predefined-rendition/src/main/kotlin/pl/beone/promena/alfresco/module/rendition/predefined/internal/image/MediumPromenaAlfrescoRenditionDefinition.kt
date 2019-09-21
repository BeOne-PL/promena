package pl.beone.promena.alfresco.module.rendition.predefined.internal.image

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.IMAGE_JPEG
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterParameters
import pl.beone.promena.transformer.converter.imagemagick.applicationmodel.imageMagickConverterTransformation

class MediumPromenaAlfrescoRenditionDefinition : AlfrescoPromenaRenditionDefinition {

    override fun getRenditionName(): String =
        "medium"

    override fun getTransformation(nodeRef: NodeRef, mediaType: MediaType): Transformation =
        getTransformation(
            mediaType,
            imageMagickConverterTransformation(IMAGE_JPEG, imageMagickConverterParameters(width = 100, height = 100))
        ) ?: throw AlfrescoPromenaRenditionTransformationNotSupportedException(nodeRef, getRenditionName(), mediaType, IMAGE_JPEG)
}