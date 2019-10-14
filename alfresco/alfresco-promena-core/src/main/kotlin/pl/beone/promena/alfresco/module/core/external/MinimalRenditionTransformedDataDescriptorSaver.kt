package pl.beone.promena.alfresco.module.core.external

import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.alfresco.service.transaction.TransactionService
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_ID
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_DATA_INDEX
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_DATA_SIZE
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_TRANSFORMATION_ID
import pl.beone.promena.alfresco.module.core.contract.DataConverter
import pl.beone.promena.alfresco.module.core.contract.TransformedDataDescriptorSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.data.NoData
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class MinimalRenditionTransformedDataDescriptorSaver(
    private val saveIfZero: Boolean,
    private val nodeService: NodeService,
    private val contentService: ContentService,
    private val namespaceService: NamespaceService,
    private val transactionService: TransactionService,
    private val dataConverter: DataConverter
) : TransformedDataDescriptorSaver {

    companion object {
        const val METADATA_ALF_PREFIX = "alf_"
    }

    override fun save(transformation: Transformation, nodeRefs: List<NodeRef>, transformedDataDescriptor: TransformedDataDescriptor): List<NodeRef> =
        transactionService.retryingTransactionHelper.doInTransaction {
            val sourceNodeRef = nodeRefs.first()

            val renditionsNodeRefs = if (transformedDataDescriptor.descriptors.isNotEmpty()) {
                handle(sourceNodeRef, transformation, transformedDataDescriptor.descriptors)
            } else {
                if (saveIfZero) {
                    handleZero(sourceNodeRef, transformation)
                } else {
                    emptyList()
                }
            }

            renditionsNodeRefs
        }

    private fun handle(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>
    ): List<NodeRef> {
        val id = generateId()

        return transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val dataSize = transformedDataDescriptors.size
            val properties = createGeneralAndThumbnailProperties(transformation.getTransformerIdsDescription()) +
                    determinePromenaProperties(id, transformation, index, dataSize) +
                    determineAlfrescoProperties(transformedDataDescriptor.metadata)

            createRenditionNode(sourceNodeRef, transformation.getTransformerIdsDescription(), properties).apply {
                if (transformedDataDescriptor.hasContent()) {
                    saveContent(transformation.determineDestinationMediaType(), transformedDataDescriptor.data)
                }
            }
        }
    }

    private fun handleZero(sourceNodeRef: NodeRef, transformation: Transformation): List<NodeRef> {
        val name = transformation.getTransformerIdsDescription()

        val properties = createGeneralAndThumbnailProperties(name) +
                determinePromenaProperties(generateId(), transformation)

        return listOf(createRenditionNode(sourceNodeRef, name, properties))
    }

    private fun generateId(): String =
        UUID.randomUUID().toString()

    private fun Transformation.getTransformerIdsDescription(): String =
        convertToStringifiedTransformationId(this)
            .joinToString(", ") { it }

    private fun Transformation.determineDestinationMediaType(): MediaType =
        transformers.last().targetMediaType

    private fun createGeneralAndThumbnailProperties(nodeName: String): Map<QName, Serializable?> =
        mapOf(
            PROP_NAME to nodeName,
            PROP_IS_INDEXED to false
        )

    private fun determinePromenaProperties(
        id: String,
        transformation: Transformation,
        transformationDataIndex: Int? = null,
        transformationDataSize: Int? = null
    ): Map<QName, Serializable?> =
        mapOf(
            PROP_ID to id,
            PROP_TRANSFORMATION to ArrayList(convertToStringifiedTransformation(transformation)), // must be mutable because Alfresco operates on original List
            PROP_TRANSFORMATION_ID to ArrayList(convertToStringifiedTransformationId(transformation)), // must be mutable because Alfresco operates on original List
            PROP_TRANSFORMATION_DATA_INDEX to transformationDataIndex,
            PROP_TRANSFORMATION_DATA_SIZE to transformationDataSize
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

    private fun determineAlfrescoProperties(metadata: Metadata): Map<QName, Serializable?> =
        metadata.getAll()
            .filter { (key) -> key.startsWith(METADATA_ALF_PREFIX) }
            .map { (key, value) -> key.removePrefix(METADATA_ALF_PREFIX) to value }
            .map { (key, value) -> QName.createQName(key, namespaceService) to value as Serializable? }
            .toMap()

    private fun createRenditionNode(sourceNodeRef: NodeRef, name: String, properties: Map<QName, Serializable?>): NodeRef =
        nodeService.createNode(
            sourceNodeRef,
            ASSOC_RENDITION,
            QName.createQName(CONTENT_MODEL_1_0_URI, name),
            TYPE_THUMBNAIL,
            properties
        ).childRef

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        contentService.getWriter(this, PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            dataConverter.saveDataInContentWriter(data, this)
        }
    }

    private fun TransformedDataDescriptor.Single.hasContent(): Boolean =
        data !is NoData
}