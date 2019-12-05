package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_EXECUTION_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_EXECUTION_IDS
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_DATA_INDEX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_DATA_SIZE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_TRANSFORMATION_ID
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.contract.node.TransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.data.NoData
import java.io.Serializable

class MinimalRenditionTransformedDataDescriptorSaver(
    private val saveIfZero: Boolean,
    private val promenaTransformationMetadataSavers: List<PromenaTransformationMetadataSaver>,
    private val dataConverter: DataConverter,
    private val serviceRegistry: ServiceRegistry
) : TransformedDataDescriptorSaver {

    override fun save(
        executionId: String,
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        transformedDataDescriptor: TransformedDataDescriptor
    ): List<NodeRef> =
        serviceRegistry.retryingTransactionHelper.doInTransaction {
            nodeRefs.forEach { addExecutionId(it, executionId) }

            val sourceNodeRef = nodeRefs.first()

            val transformedNodeRefs = if (transformedDataDescriptor.descriptors.isNotEmpty()) {
                handle(executionId, sourceNodeRef, transformation, transformedDataDescriptor.descriptors)
            } else {
                if (saveIfZero) {
                    handleZero(executionId, sourceNodeRef, transformation)
                } else {
                    emptyList()
                }
            }

            promenaTransformationMetadataSavers.forEach {
                it.save(sourceNodeRef, transformation, transformedDataDescriptor, transformedNodeRefs)
            }

            transformedNodeRefs
        }

    @Suppress("UNCHECKED_CAST")
    private fun addExecutionId(sourceNodeRef: NodeRef, executionId: String) {
        val currentExecutionIds = ((serviceRegistry.nodeService.getProperty(sourceNodeRef, PROPERTY_EXECUTION_ID) as List<String>?) ?: emptyList())
        val updatedExecutionIds = currentExecutionIds + executionId

        serviceRegistry.nodeService.setProperty(sourceNodeRef, PROPERTY_EXECUTION_IDS, updatedExecutionIds.toMutableList() as Serializable)
    }

    private fun handle(
        executionId: String,
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>
    ): List<NodeRef> =
        transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val name = transformation.getTransformerIdsDescription()
            val properties = determinePromenaProperties(name, executionId, transformation, index, transformedDataDescriptors.size)

            createRenditionNode(sourceNodeRef, name, properties).apply {
                if (transformedDataDescriptor.hasContent()) {
                    saveContent(transformation.determineDestinationMediaType(), transformedDataDescriptor.data)
                }
            }
        }

    private fun handleZero(executionId: String, sourceNodeRef: NodeRef, transformation: Transformation): List<NodeRef> {
        val name = transformation.getTransformerIdsDescription()
        val properties = determinePromenaProperties(name, executionId, transformation)

        return listOf(createRenditionNode(sourceNodeRef, name, properties))
    }

    private fun Transformation.getTransformerIdsDescription(): String =
        convertToStringifiedTransformationId(this)
            .joinToString(", ") { it }

    private fun Transformation.determineDestinationMediaType(): MediaType =
        transformers.last().targetMediaType

    private fun determinePromenaProperties(
        name: String,
        executionId: String,
        transformation: Transformation,
        transformationDataIndex: Int? = null,
        transformationDataSize: Int? = null
    ): Map<QName, Serializable?> =
        mapOf(
            PROP_NAME to name,
            PROPERTY_EXECUTION_ID to executionId,
            PROPERTY_TRANSFORMATION to convertToStringifiedTransformation(transformation).toMutableList() as Serializable, // must be mutable because Alfresco operates on original List
            PROPERTY_TRANSFORMATION_ID to convertToStringifiedTransformationId(transformation).toMutableList() as Serializable, // must be mutable because Alfresco operates on original List
            PROPERTY_TRANSFORMATION_DATA_INDEX to transformationDataIndex,
            PROPERTY_TRANSFORMATION_DATA_SIZE to transformationDataSize
        ).filterNotNullValues()

    private fun convertToStringifiedTransformation(transformation: Transformation): List<String> =
        transformation.transformers
            .map(Transformation.Single::toString)

    private fun convertToStringifiedTransformationId(transformation: Transformation): List<String> =
        transformation.transformers
            .map(Transformation.Single::transformerId)
            .map { if (it.isSubNameSet()) it.name + "-" + it.subName else it.name }

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
        filter { (_, value) -> value != null }

    private fun createRenditionNode(sourceNodeRef: NodeRef, name: String, properties: Map<QName, Serializable?>): NodeRef =
        serviceRegistry.nodeService.createNode(
            sourceNodeRef,
            ASSOC_RENDITION,
            createQName(CONTENT_MODEL_1_0_URI, name),
            TYPE_THUMBNAIL,
            properties
        ).childRef

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        serviceRegistry.contentService.getWriter(this, PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            dataConverter.saveDataInContentWriter(data, this)
        }
    }

    private fun TransformedDataDescriptor.Single.hasContent(): Boolean =
        data !is NoData
}