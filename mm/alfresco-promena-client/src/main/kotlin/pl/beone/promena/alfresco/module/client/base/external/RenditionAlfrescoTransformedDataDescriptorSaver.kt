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
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.io.Serializable

class RenditionAlfrescoTransformedDataDescriptorSaver(
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

            val renditionsNodeRefs = when {
                transformedDataDescriptor.descriptors.size > 1  ->
                    handleMany(sourceNodeRef, transformation, transformedDataDescriptor.descriptors)
                transformedDataDescriptor.descriptors.size == 1 ->
                    handleOne(sourceNodeRef, transformation, transformedDataDescriptor.descriptors.first())
                else                                            ->
                    if (saveIfZero) {
                        handleZero(sourceNodeRef, transformation)
                    } else {
                        emptyList()
                    }
            }

            logger.debug { "Created <$transformation> rendition nodes <$renditionsNodeRefs> as a child of <$sourceNodeRef>" }

            renditionsNodeRefs
        }

    private fun handleMany(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptors: List<TransformedDataDescriptor.Single>
    ): List<NodeRef> {
        val name = determineName(transformation)
        return transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
            val properties =
                determinePromenaProperties(name, transformation, index, transformedDataDescriptors.size) +
                        createContentProperty() +
                        transformedDataDescriptor.metadata.getAlfrescoProperties()

            createRenditionNode(sourceNodeRef, "$name - ${index + 1}", properties).apply {
                saveContent(determineDestinationMediaType(transformation), transformedDataDescriptor.data)
            }
        }
    }

    private fun handleOne(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        singleTransformedDataDescriptor: TransformedDataDescriptor.Single
    ): List<NodeRef> {
        val name = determineName(transformation)
        val properties = determinePromenaProperties(name, transformation, 0, 1) +
                createContentProperty() +
                singleTransformedDataDescriptor.metadata.getAlfrescoProperties()

        return listOf(createRenditionNode(sourceNodeRef, name, properties).apply {
            saveContent(determineDestinationMediaType(transformation), singleTransformedDataDescriptor.data)
        })
    }

    private fun handleZero(sourceNodeRef: NodeRef, transformation: Transformation): List<NodeRef> {
        val name = determineName(transformation)
        val properties = determinePromenaProperties(name, transformation)

        return listOf(createRenditionNode(sourceNodeRef, name, properties))
    }

    private fun determineName(transformation: Transformation): String =
        transformation.transformers.joinToString(", ") {
            if (it.transformerId.subName != null) {
                "${it.transformerId.name}-${it.transformerId.subName}"
            } else {
                it.transformerId.name
            }
        }

    private fun createContentProperty(): Map<QName, QName> =
        mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT)

    private fun determineDestinationMediaType(transformation: Transformation): MediaType =
        transformation.transformers.last().targetMediaType

    private fun determinePromenaProperties(
        name: String,
        transformation: Transformation,
        transformationIndex: Int? = null,
        transformationSize: Int? = null
    ): Map<QName, Serializable?> =
        mapOf<QName, Serializable?>(
            ContentModel.PROP_NAME to name,
            ContentModel.PROP_THUMBNAIL_NAME to name,
            ContentModel.PROP_IS_INDEXED to false,
            PromenaTransformationContentModel.PROP_TRANSFORMATION to transformation.toListDescription(),
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_INDEX to transformationIndex,
            PromenaTransformationContentModel.PROP_TRANSFORMATION_DATA_SIZE to transformationSize
        ).filterNotNullValues()

    private fun Transformation.toListDescription(): ArrayList<String> =
        ArrayList(transformers.map { it.toString() })

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
        filter { (_, value) -> value != null }

    private fun Metadata.getAlfrescoProperties(): Map<QName, Serializable?> =
        getAll()
            .filter { it.key.startsWith("alf_") }
            .map { it.key.removePrefix("alf_") to it.value }
            .map { QName.createQName(it.first, namespaceService) to it.second as Serializable? }
            .toMap()

    // TODO verify cm:created and cm:modified because now it's "unknown"
    private fun createRenditionNode(sourceNodeRef: NodeRef, name: String, properties: Map<QName, Serializable?>): NodeRef =
        nodeService.createNode(
            sourceNodeRef,
            RenditionModel.ASSOC_RENDITION,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
            ContentModel.TYPE_THUMBNAIL,
            properties
        ).childRef.apply {
            nodeService.addAspect(this, RenditionModel.ASPECT_RENDITION2, null)
            nodeService.addAspect(this, RenditionModel.ASPECT_HIDDEN_RENDITION, null)
        }

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        contentService.getWriter(this, ContentModel.PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            alfrescoDataConverter.saveDataInContentWriter(data, this)
        }
    }
}