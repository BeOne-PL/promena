package pl.beone.promena.alfresco.module.rendition.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.transformation.Transformation

interface AlfrescoPromenaRenditionDefinition {

    fun getRenditionName(): String

    @Throws(AlfrescoPromenaRenditionTransformationNotSupportedException::class)
    fun getTransformation(nodeRef: NodeRef, mediaType: MediaType): Transformation
}