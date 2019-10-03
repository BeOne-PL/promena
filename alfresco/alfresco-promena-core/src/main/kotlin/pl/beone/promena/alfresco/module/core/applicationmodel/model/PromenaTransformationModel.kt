package pl.beone.promena.alfresco.module.core.applicationmodel.model

import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_URI

object PromenaTransformationModel {

    val ASPECT_TRANSFORMATION = QName.createQName(PROMENA_MODEL_1_0_URI, "transformation")!!

    val PROP_TRANSFORMATION = QName.createQName(PROMENA_MODEL_1_0_URI, "transformation")!!
    val PROP_TRANSFORMATION_ID = QName.createQName(PROMENA_MODEL_1_0_URI, "transformationId")!!
    val PROP_TRANSFORMATION_DATA_INDEX = QName.createQName(PROMENA_MODEL_1_0_URI, "transformationDataIndex")!!
    val PROP_TRANSFORMATION_DATA_SIZE = QName.createQName(PROMENA_MODEL_1_0_URI, "transformationDataSize")!!
    val PROP_ID = QName.createQName(PROMENA_MODEL_1_0_URI, "id")!!
    val PROP_RENDITION_NAME = QName.createQName(PROMENA_MODEL_1_0_URI, "renditionName")!!
}