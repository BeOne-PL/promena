package pl.beone.promena.alfresco.module.client.base.external

import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.alfresco.service.transaction.TransactionService
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.internal.model.data.NoData
import java.io.Serializable

class MinimalRenditionAlfrescoTransformedDataDescriptorSaver(
    private val saveIfZero: Boolean,
    private val nodeService: NodeService,
    private val contentService: ContentService,
    private val namespaceService: NamespaceService,
    private val transactionService: TransactionService,
    private val alfrescoDataConverter: AlfrescoDataConverter
) : AlfrescoTransformedDataDescriptorSaver {

    override fun save(
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        transformedDataDescriptor: TransformedDataDescriptor,
        renditionName: String?
    ): List<NodeRef> =
        transactionService.retryingTransactionHelper.doInTransaction {
            val sourceNodeRef = nodeRefs.first()

            val renditionsNodeRefs = if (transformedDataDescriptor.descriptors.isNotEmpty()) {
                handle(sourceNodeRef, transformation, transformedDataDescriptor.descriptors, renditionName)
            } else {
                if (saveIfZero) {
                    handleZero(sourceNodeRef, transformation, renditionName)
                } else {
                    emptyList()
                }
            }

            renditionsNodeRefs
        }

    private fun handle(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>,
        renditionName: String?
    ): List<NodeRef> {
        return transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val dataSize = transformedDataDescriptors.size
            val properties = createGeneralAndThumbnailProperties(createNodeName(transformation)) +
                    determinePromenaProperties(transformation, index, dataSize) +
                    determineAlfrescoProperties(transformedDataDescriptor.metadata)

            createRenditionNode(sourceNodeRef, renditionName ?: determineAssociationName(transformation, index, dataSize), properties).apply {
                if (transformedDataDescriptor.hasContent()) {
                    saveContent(transformation.determineDestinationMediaType(), transformedDataDescriptor.data)
                }
            }
        }
    }

    private fun handleZero(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        renditionName: String?
    ): List<NodeRef> {
        val properties = createGeneralAndThumbnailProperties(createNodeName(transformation)) +
                determinePromenaProperties(transformation)

        return listOf(createRenditionNode(sourceNodeRef, renditionName ?: determineAssociationName(transformation, -1, 0), properties))
    }

    private fun createNodeName(transformation: Transformation): String =
        transformation.transformers.joinToString(", ") {
            if (it.transformerId.subName != null) {
                "${it.transformerId.name}_${it.transformerId.subName}"
            } else {
                it.transformerId.name
            }
        }

    private fun determineAssociationName(transformation: Transformation, dataIndex: Int, dataSize: Int): String {
        val baseName = createNodeName(transformation)
        return if (dataSize > 1) {
            "$baseName - $dataIndex"
        } else {
            baseName
        }
    }

    private fun Transformation.determineDestinationMediaType(): MediaType =
        transformers.last().targetMediaType

    private fun createGeneralAndThumbnailProperties(nodeName: String): Map<QName, Serializable?> =
        mapOf(
            ContentModel.PROP_NAME to nodeName,
            ContentModel.PROP_IS_INDEXED to false,
            ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT
        )

    private fun determinePromenaProperties(
        transformation: Transformation,
        transformationDataIndex: Int? = null,
        transformationDataSize: Int? = null
    ): Map<QName, Serializable?> =
        mapOf(
            PromenaTransformationContentModel.PROP_TRANSFORMATION to
                    ArrayList(convertToStringifiedDescriptions(transformation)), // must be mutable because Alfresco operates on original List
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to transformationDataIndex,
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to transformationDataSize
        ).filterNotNullValues()

    private fun convertToStringifiedDescriptions(transformation: Transformation): List<String> =
        transformation.transformers.map { it.toString() }

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
        filter { (_, value) -> value != null }

    private fun determineAlfrescoProperties(metadata: Metadata): Map<QName, Serializable?> =
        metadata.getAll()
            .filter { it.key.startsWith("alf_") }
            .map { it.key.removePrefix("alf_") to it.value }
            .map { QName.createQName(it.first, namespaceService) to it.second as Serializable? }
            .toMap()

    private fun createRenditionNode(sourceNodeRef: NodeRef, name: String, properties: Map<QName, Serializable?>): NodeRef =
        nodeService.createNode(
            sourceNodeRef,
            RenditionModel.ASSOC_RENDITION,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
            ContentModel.TYPE_THUMBNAIL,
            properties
        ).childRef

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        contentService.getWriter(this, ContentModel.PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            alfrescoDataConverter.saveDataInContentWriter(data, this)
        }
    }

    private fun TransformedDataDescriptor.Single.hasContent(): Boolean =
        data !is NoData
}