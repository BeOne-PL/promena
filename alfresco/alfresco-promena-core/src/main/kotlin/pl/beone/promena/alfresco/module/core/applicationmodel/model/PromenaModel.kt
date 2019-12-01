package pl.beone.promena.alfresco.module.core.applicationmodel.model

import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_URI

object PromenaModel {

    @JvmField
    val ASPECT_TRANSFORMATION = createQName(PROMENA_MODEL_1_0_URI, "transformation")!!
    @JvmField
    val PROPERTY_TRANSFORMATION = createQName(PROMENA_MODEL_1_0_URI, "transformation")!!
    @JvmField
    val PROPERTY_TRANSFORMATION_ID = createQName(PROMENA_MODEL_1_0_URI, "transformationId")!!
    @JvmField
    val PROPERTY_TRANSFORMATION_DATA_INDEX = createQName(PROMENA_MODEL_1_0_URI, "transformationDataIndex")!!
    @JvmField
    val PROPERTY_TRANSFORMATION_DATA_SIZE = createQName(PROMENA_MODEL_1_0_URI, "transformationDataSize")!!
    @JvmField
    val PROPERTY_ID = createQName(PROMENA_MODEL_1_0_URI, "id")!!
    @JvmField
    val PROPERTY_RENDITION_NAME = createQName(PROMENA_MODEL_1_0_URI, "renditionName")!!
}