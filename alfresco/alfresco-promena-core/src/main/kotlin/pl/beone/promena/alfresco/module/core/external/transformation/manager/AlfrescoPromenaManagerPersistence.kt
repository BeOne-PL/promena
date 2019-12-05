package pl.beone.promena.alfresco.module.core.external.transformation.manager

import org.alfresco.model.ContentModel.*
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.repo.security.authentication.AuthenticationUtil
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.ASSOCIATION_TRANSFORMATIONS
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.PROPERTY_EXECUTION_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.PROPERTY_FINISH_DATE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.PROPERTY_NODE_REFS
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.PROPERTY_START_DATE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.PROPERTY_THROWABLE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.TYPE_COORDINATOR
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaManagerModel.TYPE_TRANSFORMATION
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaNamespace.PROMENA_MANAGER_MODEL_1_0_URI
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.core.contract.serialization.SerializationService
import java.io.Serializable
import java.util.*

internal class AlfrescoPromenaManagerPersistence(
    private val serializationService: SerializationService,
    private val serviceRegistry: ServiceRegistry
) {

    companion object {
        private const val PROMENA_COORDINATOR_NAME = "Promena"
    }

    private val promenaCoordinatorNode: NodeRef by lazy { getCoordinatorNode() ?: createCoordinatorNode() }

    fun getResult(executionId: String): TransformationExecutionResult? =
        runInTransactionAsAdmin(true) {
            runForTransformation(executionId) { transformationNodeRef ->
                throwExceptionIfTransformationEndedWithError(transformationNodeRef)
                getTransformationExecutionResult(transformationNodeRef)
            }
        }

    private fun throwExceptionIfTransformationEndedWithError(transformationNodeRef: NodeRef): Throwable? =
        transformationNodeRef.getProperty<List<Int>>(PROPERTY_THROWABLE)
            ?.let { exception -> throw serializationService.deserialize(ByteArray(exception.size) { exception[it].toByte() }, Throwable::class.java) }

    private fun getTransformationExecutionResult(transformationNodeRef: NodeRef): TransformationExecutionResult? =
        transformationNodeRef.getProperty<List<String>>(PROPERTY_NODE_REFS)
            ?.map(::NodeRef)
            ?.let(::TransformationExecutionResult)

    fun startTransformation(transformationExecution: TransformationExecution) {
        val executionId = transformationExecution.id
        runInTransactionAsAdmin(false) {
            serviceRegistry.nodeService.createNode(
                promenaCoordinatorNode,
                ASSOCIATION_TRANSFORMATIONS,
                createQName(PROMENA_MANAGER_MODEL_1_0_URI, executionId),
                TYPE_TRANSFORMATION,
                mapOf<QName, Serializable>(
                    PROP_NAME to executionId,
                    PROPERTY_EXECUTION_ID to executionId,
                    PROPERTY_START_DATE to Date(),
                    PROP_IS_INDEXED to false,
                    PROP_IS_CONTENT_INDEXED to false
                )
            )
        }
    }

    fun completeTransformation(transformationExecution: TransformationExecution, result: TransformationExecutionResult): Boolean =
        runInTransactionAsAdmin(false) {
            runForTransformation(transformationExecution.id) { transformationNodeRef ->
                transformationNodeRef.setProperty(PROPERTY_NODE_REFS, result.nodeRefs.map { it.toString() })
                transformationNodeRef.setProperty(PROPERTY_FINISH_DATE, Date())

                true
            } ?: false
        }

    fun completeErrorTransformation(transformationExecution: TransformationExecution, throwable: Throwable): Boolean =
        runInTransactionAsAdmin(false) {
            runForTransformation(transformationExecution.id) { transformationNodeRef ->
                transformationNodeRef.setProperty(PROPERTY_FINISH_DATE, Date())
                transformationNodeRef.setProperty(PROPERTY_THROWABLE, serializationService.serialize(throwable).map(Byte::toInt))

                true
            } ?: false
        }

    private fun NodeRef.setProperty(qName: QName, value: Any) {
        serviceRegistry.nodeService.setProperty(this, qName, value as Serializable)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> NodeRef.getProperty(qName: QName): T? =
        serviceRegistry.nodeService.getProperty(this, qName) as T?

    private fun <T> runForTransformation(executionId: String, toExecute: (NodeRef) -> T): T? =
        serviceRegistry.nodeService.getChildAssocsByPropertyValue(promenaCoordinatorNode, PROPERTY_EXECUTION_ID, executionId)
            .map { it.childRef }
            .firstOrNull()
            ?.let(toExecute)

    private fun <T> runInTransactionAsAdmin(readOnly: Boolean, toRun: () -> T): T =
        AuthenticationUtil.runAs({
            serviceRegistry.retryingTransactionHelper.doInTransaction(toRun, readOnly, false)
        }, AuthenticationUtil.getAdminUserName())

    private fun getCoordinatorNode(): NodeRef? =
        serviceRegistry.nodeService.getChildByName(
            getCompanyHomeNodeRef(),
            ASSOC_CONTAINS,
            PROMENA_COORDINATOR_NAME
        )

    private fun createCoordinatorNode(): NodeRef =
        serviceRegistry.nodeService.createNode(
            getCompanyHomeNodeRef(),
            ASSOC_CONTAINS,
            createQName(PROMENA_MANAGER_MODEL_1_0_URI, PROMENA_COORDINATOR_NAME),
            TYPE_COORDINATOR,
            mapOf(
                PROP_NAME to PROMENA_COORDINATOR_NAME,
                PROP_IS_INDEXED to false,
                PROP_IS_CONTENT_INDEXED to false
            )
        ).childRef

    private fun getCompanyHomeNodeRef(): NodeRef =
        serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)
}