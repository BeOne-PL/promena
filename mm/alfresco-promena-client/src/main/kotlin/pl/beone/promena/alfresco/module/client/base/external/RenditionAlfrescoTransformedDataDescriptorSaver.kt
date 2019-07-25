package pl.beone.promena.alfresco.module.client.base.external

import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.alfresco.service.transaction.TransactionService
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.model.PromenaTransformationContentModel
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.io.Serializable

class RenditionAlfrescoTransformedDataDescriptorSaver(private val saveIfZero: Boolean,
                                                      private val nodeService: NodeService,
                                                      private val contentService: ContentService,
                                                      private val namespaceService: NamespaceService,
                                                      private val transactionService: TransactionService,
                                                      private val alfrescoDataConverter: AlfrescoDataConverter)
    : AlfrescoTransformedDataDescriptorSaver {

    companion object {
        private val logger = LoggerFactory.getLogger(RenditionAlfrescoTransformedDataDescriptorSaver::class.java)
    }

    override fun save(transformation: Transformation,
                      nodeRefs: List<NodeRef>,
                      transformedDataDescriptor: TransformedDataDescriptor): List<NodeRef> =
            transactionService.retryingTransactionHelper.doInTransaction {
                val sourceNodeRef = nodeRefs.first()

                val renditionsNodeRefs = when {
                    transformedDataDescriptor.descriptors.size > 1  ->
                        handleMany(sourceNodeRef, transformation, transformedDataDescriptor.descriptors)
                    transformedDataDescriptor.descriptors.size == 1 ->
                        handleOne(sourceNodeRef, transformation, transformedDataDescriptor.descriptors.first())
                    else                                 ->
                        if (saveIfZero) {
                            handleZero(sourceNodeRef, transformation)
                        } else {
                            emptyList()
                        }
                }

                logger.debug("Created <{}> rendition nodes <{}> as a child of <{}>", transformation, renditionsNodeRefs, sourceNodeRef)

                renditionsNodeRefs
            }

    private fun handleMany(sourceNodeRef: NodeRef,
                           transformerId: String,
                           targetMediaType: MediaType,
                           transformedDataDescriptors: List<TransformedDataDescriptor.Single>): List<NodeRef> =
            transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
                val properties =
                        determineTransformationProperties(transformerId, index, transformedDataDescriptors.size) +
                        createContentProperty() +
                        transformedDataDescriptor.metadata.getAlfrescoProperties()

                createRenditionNode(sourceNodeRef, "$transformerId-${index + 1}", properties).apply {
                    saveContent(targetMediaType, transformedDataDescriptor.data)
                }
            }

    private fun handleOne(sourceNodeRef: NodeRef,
                          transformerId: String,
                          targetMediaType: MediaType,
                          transformedDataDescriptor: TransformedDataDescriptor.Single): List<NodeRef> {
        val properties = determineTransformationProperties(transformerId, 0, 1) +
                         createContentProperty() +
                         transformedDataDescriptor.metadata.getAlfrescoProperties()

        return listOf(createRenditionNode(sourceNodeRef, transformerId, properties).apply {
            saveContent(targetMediaType, transformedDataDescriptor.data)
        })
    }

    private fun handleZero(sourceNodeRef: NodeRef, transformerId: String): List<NodeRef> {
        val properties =
                determineTransformationProperties(transformerId, null, 0)

        return listOf(createRenditionNode(sourceNodeRef, transformerId, properties))
    }

    private fun createContentProperty(): Map<QName, QName> =
            mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT)

    private fun determineTransformationProperties(transformerId: String,
                                                  transformationIndex: Int?,
                                                  transformationSize: Int): Map<QName, Serializable?> =
            mapOf<QName, Serializable?>(ContentModel.PROP_NAME to transformerId,
                                        ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                                        ContentModel.PROP_IS_INDEXED to false,
                                        PromenaTransformationContentModel.PROP_TRANSFORMATION_INDEX to transformationIndex,
                                        PromenaTransformationContentModel.PROP_TRANSFORMATION_SIZE to transformationSize)
                    .filterNotNullValues()

    private fun <T, U> Map<T, U>.filterNotNullValues(): Map<T, U> =
            filter { (_, value) -> value != null }

    private fun Metadata.getAlfrescoProperties(): Map<QName, Serializable?> =
            this.getAll()
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