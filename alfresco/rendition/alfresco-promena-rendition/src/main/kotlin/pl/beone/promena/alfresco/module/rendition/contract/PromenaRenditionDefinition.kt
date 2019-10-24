package pl.beone.promena.alfresco.module.rendition.contract

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.transformation.Transformation

interface PromenaRenditionDefinition {

    fun getRenditionName(): String

    fun getTargetMediaType(): MediaType

    fun getTransformation(mediaType: MediaType): Transformation

    fun getPlaceHolderResourcePath(): String? = null

    fun getMimeAwarePlaceHolderResourcePath(): String? = null
}