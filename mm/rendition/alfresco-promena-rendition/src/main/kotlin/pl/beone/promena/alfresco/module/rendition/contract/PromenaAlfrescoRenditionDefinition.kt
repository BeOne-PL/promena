package pl.beone.promena.alfresco.module.rendition.contract

import pl.beone.promena.transformer.contract.transformation.Transformation

interface PromenaAlfrescoRenditionDefinition {

    fun getRenditionName(): String

    fun getTransformation(): Transformation
}