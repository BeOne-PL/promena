package pl.beone.promena.alfresco.module.client.base.applicationmodel.model

import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaNamespace.PROMENA_MODEL_1_0_URI

object PromenaTransformationContentModel {
    val ASPECT_TRANSFORMATION = QName.createQName(PROMENA_MODEL_1_0_URI, "transformation")
    val PROP_TRANSFORMATION_INDEX = QName.createQName(PROMENA_MODEL_1_0_URI, "transformationIndex")
    val PROP_TRANSFORMATION_SIZE = QName.createQName(PROMENA_MODEL_1_0_URI, "transformationSize")
}