package pl.beone.promena.alfresco.module.client.base.external

import mu.KotlinLogging
import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.alfresco.service.transaction.TransactionService
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel
import pl.beone.promena.alfresco.module.client.base.applicationmodel.parameters.PromenaParametersAlfrescoConstants
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

    companion object {
        private val logger = KotlinLogging.logger {}
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

            logger.debug { "Created <$transformation> rendition nodes <$renditionsNodeRefs> as a child of <$sourceNodeRef>" }

            renditionsNodeRefs
        }

    private fun handle(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>
    ): List<NodeRef> {
        return transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val dataSize = transformedDataDescriptors.size
            val properties = createGeneralAndThumbnailProperties(transformation.createName()) +
                    determinePromenaProperties(transformation, index, dataSize) +
                    transformedDataDescriptor.metadata.determineAlfrescoProperties()

            createRenditionNode(sourceNodeRef, determineAssociationName(transformation, index, dataSize), properties).apply {
                if (transformedDataDescriptor.hasContent()) {
                    saveContent(transformation.determineDestinationMediaType(), transformedDataDescriptor.data)
                }
            }
        }
    }

    private fun handleZero(sourceNodeRef: NodeRef, transformation: Transformation): List<NodeRef> {
        val properties = createGeneralAndThumbnailProperties(transformation.createName()) +
                determinePromenaProperties(transformation)

        return listOf(createRenditionNode(sourceNodeRef, determineAssociationName(transformation, -1, 0), properties))
    }

    private fun Transformation.createName(): String =
        transformers.joinToString(", ") {
            if (it.transformerId.subName != null) {
                "${it.transformerId.name}|${it.transformerId.subName}"
            } else {
                it.transformerId.name
            }
        }

    private fun determineAssociationName(transformation: Transformation, dataIndex: Int, dataSize: Int): String {
        val baseName = try {
            transformation.getAlfrescoRenditionName()
        } catch (e: NoSuchElementException) {
            transformation.createName()
        }

        return if (dataSize > 1) {
            "$baseName - $dataIndex"
        } else {
            baseName
        }
    }

    private fun Transformation.getAlfrescoRenditionName(): String =
        transformers.map {
            it.parameters
                .getParameters(PromenaParametersAlfrescoConstants.PARAMETERS_ALFRESCO)
                .get(PromenaParametersAlfrescoConstants.PARAMETERS_ALFRESCO_RENDITION_NAME, String::class.java)
        }.last()

    private fun Transformation.determineDestinationMediaType(): MediaType =
        transformers.last().targetMediaType

    private fun createGeneralAndThumbnailProperties(name: String): Map<QName, Serializable?> =
        mapOf<QName, Serializable?>(
            ContentModel.PROP_NAME to name,
            ContentModel.PROP_IS_INDEXED to false,
            ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT
        )

    private fun determinePromenaProperties(
        transformation: Transformation,
        transformationDataIndex: Int? = null,
        transformationDataSize: Int? = null
    ): Map<QName, Serializable?> =
        mapOf(
            PromenaTransformationContentModel.PROP_TRANSFORMATION to transformation.toListDescription(),
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to transformationDataIndex,
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to transformationDataSize
        ).filterNotNullValues()

    private fun Transformation.toListDescription(): ArrayList<String> =
        ArrayList(transformers.map { it.toString() })

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
        filter { (_, value) -> value != null }

    private fun Metadata.determineAlfrescoProperties(): Map<QName, Serializable?> =
        getAll()
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