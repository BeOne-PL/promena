package pl.beone.promena.alfresco.module.rendition.internal.transformation

import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationMetadataMapperElement
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationMetadataMapperElement
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataMapper

class RenditionPromenaTransformationMetadataMapper : PromenaTransformationMetadataMapper {

    companion object {
        val PROP_RENDITION_NAME_PREFIXED = PROMENA_MODEL_1_0_PREFIX + ":" + PROP_RENDITION_NAME.localName
    }

    override fun getElements(): List<TransformationMetadataMapperElement> =
        listOf(
            transformationMetadataMapperElement(PROP_RENDITION_NAME_PREFIXED, PROP_RENDITION_NAME)
        )
}