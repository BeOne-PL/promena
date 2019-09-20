package pl.beone.promena.alfresco.module.rendition.contract

import pl.beone.promena.transformer.contract.transformation.Transformation

interface AlfrescoPromenaRenditionDefinition {

    fun getRenditionName(): String

    fun getTransformation(): Transformation
}