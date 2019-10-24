package pl.beone.promena.alfresco.module.rendition.internal.transformation.definition

import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_PREFIX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.contract.transformation.definition.PromenaTransformationMetadataMappingDefinition

class PromenaRenditionNamePromenaTransformationMetadataMappingDefinition : PromenaTransformationMetadataMappingDefinition {

    companion object {
        val PROP_RENDITION_NAME_PREFIXED = PROMENA_MODEL_1_0_PREFIX + ":" + PROP_RENDITION_NAME.localName
    }

    override fun getKey(): String =
        PROP_RENDITION_NAME_PREFIXED

    override fun getProperty(): QName =
        PROP_RENDITION_NAME
}