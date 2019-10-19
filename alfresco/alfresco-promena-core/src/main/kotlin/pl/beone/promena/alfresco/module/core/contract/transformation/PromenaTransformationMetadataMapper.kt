package pl.beone.promena.alfresco.module.core.contract.transformation

import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationMetadataMapperElement

interface PromenaTransformationMetadataMapper {

    fun getElements(): List<TransformationMetadataMapperElement>
}