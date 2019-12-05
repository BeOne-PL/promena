package pl.beone.promena.alfresco.module.core.applicationmodel.model

import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MANAGER_MODEL_1_0_URI

object PromenaManagerModel {

    @JvmField
    val TYPE_COORDINATOR = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "coordinator")!!
    @JvmField
    val ASSOCIATION_TRANSFORMATIONS = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "transformations")!!

    @JvmField
    val TYPE_TRANSFORMATION = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "transformation")!!
    @JvmField
    val PROPERTY_EXECUTION_ID = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "executionId")!!
    @JvmField
    val PROPERTY_START_DATE = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "startDate")!!
    @JvmField
    val PROPERTY_FINISH_DATE = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "finishDate")!!
    @JvmField
    val PROPERTY_THROWABLE = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "throwable")!!
    @JvmField
    val PROPERTY_NODE_REFS = createQName(PROMENA_MANAGER_MODEL_1_0_URI, "nodeRefs")!!
}