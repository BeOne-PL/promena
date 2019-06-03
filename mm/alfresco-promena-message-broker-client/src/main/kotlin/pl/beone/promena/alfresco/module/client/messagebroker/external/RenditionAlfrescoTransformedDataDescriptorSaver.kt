package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.model.ContentModel
import org.alfresco.model.RenditionModel
import org.alfresco.repo.rendition2.RenditionService2Impl
import org.alfresco.service.cmr.repository.ContentData
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata
import java.io.Serializable

class RenditionAlfrescoTransformedDataDescriptorSaver(private val saveIfZero: Boolean,
                                                      private val nodeService: NodeService,
                                                      private val contentService: ContentService,
                                                      private val namespaceService: NamespaceService,
                                                      private val alfrescoDataConverter: AlfrescoDataConverter)
    : AlfrescoTransformedDataDescriptorSaver {

    companion object {
        private val logger = LoggerFactory.getLogger(RenditionAlfrescoTransformedDataDescriptorSaver::class.java)
    }

    override fun save(transformerId: String,
                      nodeRefs: List<NodeRef>,
                      targetMediaType: MediaType,
                      transformedDataDescriptors: List<TransformedDataDescriptor>): List<NodeRef> {
        val sourceNodeRef = nodeRefs.first()
        val sourceNodeRefContentHashCode = sourceNodeRef.getSourceContentHashCode()

        val renditionsNodeRefs = if (transformedDataDescriptors.size > 1) {
            handleMany(sourceNodeRef,
                       sourceNodeRefContentHashCode,
                       transformerId,
                       targetMediaType,
                       transformedDataDescriptors)
        } else if (transformedDataDescriptors.size == 1) {
            handleOne(sourceNodeRef,
                      sourceNodeRefContentHashCode,
                      transformerId,
                      targetMediaType,
                      transformedDataDescriptors.first())
        } else {
            if (saveIfZero) {
                handleZero(sourceNodeRef, sourceNodeRefContentHashCode, transformerId)
            } else {
                emptyList()
            }
        }

        logger.debug("Created <{}> rendition nodes <{}> as a child of <{}>",
                     transformerId,
                     renditionsNodeRefs,
                     sourceNodeRef)

        return renditionsNodeRefs
    }

    private fun handleMany(sourceNodeRef: NodeRef,
                           sourceNodeRefContentHashCode: Int,
                           transformerId: String,
                           targetMediaType: MediaType,
                           transformedDataDescriptors: List<TransformedDataDescriptor>): List<NodeRef> =
            transformedDataDescriptors.mapIndexed { index, transformedDataDescriptor ->
                val properties = determineAllPropertiesWithContent(transformerId,
                                                                   sourceNodeRefContentHashCode,
                                                                   transformedDataDescriptor)

                createRenditionNode(sourceNodeRef, "$transformerId-${index + 1}", properties).apply {
                    saveContent(targetMediaType, transformedDataDescriptor.data)
                }
            }

    private fun handleOne(sourceNodeRef: NodeRef,
                          sourceNodeRefContentHashCode: Int,
                          transformerId: String,
                          targetMediaType: MediaType,
                          transformedDataDescriptor: TransformedDataDescriptor): List<NodeRef> {
        val properties = determineAllPropertiesWithContent(transformerId,
                                                           sourceNodeRefContentHashCode,
                                                           transformedDataDescriptor)

        return listOf(createRenditionNode(sourceNodeRef, transformerId, properties).apply {
            saveContent(targetMediaType, transformedDataDescriptor.data)
        })
    }

    private fun handleZero(sourceNodeRef: NodeRef, sourceNodeRefContentHashCode: Int, transformerId: String): List<NodeRef> {
        val properties = createRenditionPropertiesWithoutContent(transformerId,
                                                                 sourceNodeRefContentHashCode)

        return listOf(createRenditionNode(sourceNodeRef, transformerId, properties))
    }

    private fun NodeRef.getSourceContentHashCode(): Int {
        val contentData = DefaultTypeConverter.INSTANCE.convert(
                ContentData::class.java,
                nodeService.getProperty(this, ContentModel.PROP_CONTENT)
        )

        return if (contentData != null) {
            // Originally we used the contentData URL, but that is not enough if the mimetype changes.
            (contentData.contentUrl + contentData.mimetype).hashCode()
        } else {
            RenditionService2Impl.SOURCE_HAS_NO_CONTENT
        }
    }

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

    private fun determineAllPropertiesWithContent(transformerId: String,
                                                  sourceNodeRefContentHashCode: Int,
                                                  transformedDataDescriptor: TransformedDataDescriptor): Map<QName, Serializable?> =
            createRenditionPropertiesWithoutContent(transformerId, sourceNodeRefContentHashCode) +
            mapOf(ContentModel.PROP_CONTENT_PROPERTY_NAME to ContentModel.PROP_CONTENT) +
            transformedDataDescriptor.metadata.getAlfrescoProperties()

    private fun createRenditionPropertiesWithoutContent(transformerId: String, sourceNodeContentHashCode: Int): Map<QName, Serializable> {
        return mapOf(ContentModel.PROP_NAME to transformerId,
                     ContentModel.PROP_THUMBNAIL_NAME to transformerId,
                     ContentModel.PROP_IS_INDEXED to false,
                     RenditionModel.PROP_RENDITION_CONTENT_HASH_CODE to sourceNodeContentHashCode)
    }

    private fun Metadata.getAlfrescoProperties(): Map<QName, Serializable?> =
            this.getAll()
                    .filter { it.key.startsWith("alf_") }
                    .map { it.key.removePrefix("alf_") to it.value }
                    .map { QName.createQName(it.first, namespaceService) to it.second as Serializable? }
                    .toMap()

    private fun NodeRef.saveContent(targetMediaType: MediaType, data: Data) {
        contentService.getWriter(this, ContentModel.PROP_CONTENT, true).apply {
            mimetype = targetMediaType.mimeType
            alfrescoDataConverter.saveDataInContentWriter(data, this)
        }
    }
}