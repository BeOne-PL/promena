package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.ChildAssociationRef
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

            val primaryNodeRef = nodeRefs.first()
            val theRestNodeRefs = nodeRefs - primaryNodeRef

            val transformedNodeRefs = if (transformedDataDescriptor.descriptors.isNotEmpty()) {
                handle(executionId, primaryNodeRef, theRestNodeRefs, transformation, transformedDataDescriptor.descriptors)
            } else {
                if (saveIfZero) {
                    handleZero(executionId, primaryNodeRef, theRestNodeRefs, transformation)
                } else {
                    emptyList()
                }
            }

            saveMetadata(nodeRefs, transformation, transformedDataDescriptor, transformedNodeRefs)

            transformedNodeRefs
        }

    @Suppress("UNCHECKED_CAST")
    private fun addExecutionId(sourceNodeRef: NodeRef, executionId: String) {
        val currentExecutionIds = ((serviceRegistry.nodeService.getProperty(sourceNodeRef, PROPERTY_EXECUTION_IDS) as List<String>?) ?: emptyList())
        val updatedExecutionIds = currentExecutionIds + executionId

        serviceRegistry.nodeService.setProperty(sourceNodeRef, PROPERTY_EXECUTION_IDS, updatedExecutionIds.toMutableList() as Serializable)
    }

    private fun handle(
        executionId: String,
        primaryNodeRef: NodeRef,
        theRestNodeRefs: List<NodeRef>,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>
    ): List<NodeRef> =
        transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val name = getTransformerIdsDescription(transformation)
            val properties = determinePromenaProperties(name, executionId, transformation, index, transformedDataDescriptors.size)

            val qname = name.toContentQName()
            createRenditionNode(primaryNodeRef, qname, properties)
                .also {
                    if (transformedDataDescriptor.hasContent()) {
                        it.saveContent(transformation.destinationMediaType(), transformedDataDescriptor.data)
                    }
                }.also { theRestNodeRefs.addRenditionAssociations(it, qname) }
        }

    private fun handleZero(executionId: String, primaryNodeRef: NodeRef, theRestNodeRefs: List<NodeRef>, transformation: Transformation): List<NodeRef> {
        val name = getTransformerIdsDescription(transformation)
        val properties = determinePromenaProperties(name, executionId, transformation)

        val qname = name.toContentQName()
        return listOf(
            createRenditionNode(primaryNodeRef, qname, properties)
                .also { theRestNodeRefs.addRenditionAssociations(it, qname) }
        )
    }

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

    private fun getTransformerIdsDescription(transformation: Transformation): String =
        convertToStringifiedTransformationId(transformation)
            .joinToString(", ") { it }

    private fun convertToStringifiedTransformation(transformation: Transformation): List<String> =
        transformation.transformers
            .map(Transformation.Single::toString)

    private fun convertToStringifiedTransformationId(transformation: Transformation): List<String> =
        transformation.transformers
            .map(Transformation.Single::transformerId)
            .map { if (it.isSubNameSet()) it.name + "-" + it.subName else it.name }

    private fun createRenditionNode(sourceNodeRef: NodeRef, qname: QName, properties: Map<QName, Serializable?>): NodeRef =
        serviceRegistry.nodeService.createNode(sourceNodeRef, ASSOC_RENDITION, qname, TYPE_THUMBNAIL, properties).childRef

    private fun saveMetadata(
        nodeRefs: List<NodeRef>,
        transformation: Transformation,
        transformedDataDescriptor: TransformedDataDescriptor,
        transformedNodeRefs: List<NodeRef>
    ) {
        promenaTransformationMetadataSavers.forEach {
            it.save(nodeRefs, transformation, transformedDataDescriptor, transformedNodeRefs)
        }
    }

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        serviceRegistry.contentService.getWriter(this, PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            dataConverter.saveDataInContentWriter(data, this)
        }
    }

    private fun Transformation.destinationMediaType(): MediaType =
        transformers.last().targetMediaType

    private fun TransformedDataDescriptor.Single.hasContent(): Boolean =
        data !is NoData

    private fun String.toContentQName(): QName =
        createQName(CONTENT_MODEL_1_0_URI, this)

    // using ACLEntryVoterException addChild with parentRefs as List causes ACLEntryVoterException: 11050020 The specified parameter is not a NodeRef or ChildAssociationRef
    private fun List<NodeRef>.addRenditionAssociations(nodeRef: NodeRef, qname: QName): List<ChildAssociationRef> =
        this.map { serviceRegistry.nodeService.addChild(it, nodeRef, ASSOC_RENDITION, qname) }

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
        filterValues { it != null }
}