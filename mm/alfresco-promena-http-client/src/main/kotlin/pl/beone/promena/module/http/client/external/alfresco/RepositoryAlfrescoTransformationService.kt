package pl.beone.promena.module.http.client.external.alfresco

import org.alfresco.model.ContentModel
import org.alfresco.service.cmr.dictionary.DictionaryService
import org.alfresco.service.cmr.model.FileFolderService
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.namespace.NamespaceService
import org.alfresco.service.namespace.QName
import org.slf4j.LoggerFactory
import pl.beone.promena.lib.http.client.applicationmodel.exception.TransformationValidationException
import pl.beone.promena.lib.http.client.contract.transformation.TransformationServerService
import pl.beone.promena.module.http.client.applicationmodel.descriptor.NodeDescriptor
import pl.beone.promena.module.http.client.contract.alfresco.AlfrescoTransformationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.Parameters
import java.io.Serializable
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

class RepositoryAlfrescoTransformationService(private val transformationServerService: TransformationServerService,
                                              private val alfrescoDataConverter: AlfrescoFileDataConverter,
                                              private val nodeService: NodeService,
                                              private val fileFolderService: FileFolderService,
                                              private val dictionaryService: DictionaryService,
                                              private val namespaceService: NamespaceService,
                                              private val contentService: ContentService)
    : AlfrescoTransformationService {

    companion object {
        private val logger = LoggerFactory.getLogger(RepositoryAlfrescoTransformationService::class.java)

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss")!!
    }

    override fun transformToNodes(transformerId: String,
                                  nodeDescriptors: List<NodeDescriptor>,
                                  targetNodeDescriptors: List<NodeDescriptor>,
                                  targetMediaType: MediaType,
                                  parameters: Parameters,
                                  timeout: Long?) {
        logger.info("Transforming <{}> <{}, {}, {}> from nodes <{}> to nodes <{}>...",
                    transformerId,
                    targetMediaType,
                    parameters.getAll(),
                    timeout,
                    nodeDescriptors,
                    targetNodeDescriptors)

        val measuredTimeMs = measureTimeMillis {
            targetNodeDescriptors.throwIfAnyNodeDoesNotExist { TransformationValidationException("Target node <$it> doesn't exist") }

            val transformedDataDescriptors =
                    validateAndTransform(transformerId, nodeDescriptors, targetMediaType, parameters, timeout)

            if (targetNodeDescriptors.size != transformedDataDescriptors.size) {
                throw TransformationValidationException("You passed <${targetNodeDescriptors.size}> target nodes but transformation returned <${transformedDataDescriptors.size}>")
            }

            transformedDataDescriptors.saveInNodes(targetNodeDescriptors, targetMediaType)
        }

        logger.info("Transformed <{}> <{}, {}, {}> from nodes <{}> to nodes <{}> in <{} s>",
                    transformerId,
                    targetMediaType,
                    parameters.getAll(),
                    timeout,
                    nodeDescriptors,
                    targetNodeDescriptors,
                    measuredTimeMs)
    }

    override fun transformToFolder(transformerId: String,
                                   nodeDescriptors: List<NodeDescriptor>,
                                   targetFolderNodeRef: NodeRef,
                                   targetMediaType: MediaType,
                                   parameters: Parameters,
                                   timeout: Long?,
                                   targetType: QName?,
                                   targetContentProperty: QName?,
                                   namePattern: String?): List<NodeRef> {
        logger.info("Transforming <{}> <{}, {}, {}> from nodes <{}> to folder <{}> <{}, {}, {}>...",
                    transformerId,
                    targetMediaType,
                    parameters.getAll(),
                    timeout,
                    nodeDescriptors,
                    targetFolderNodeRef,
                    targetType,
                    targetContentProperty,
                    namePattern)

        val determinedNamePattern =
                namePattern ?: "Result ($transformerId) [${LocalDateTime.now().format(dateTimeFormatter)}] - \${index}"
        validateNamePattern(determinedNamePattern)

        val (measuredTimeMs, targetNodeDescriptors) = measureTimeMillisWithResult {
            targetFolderNodeRef.throwIfNotExist { TransformationValidationException("Folder node <$it> doesn't exist") }
            targetFolderNodeRef.throwIfNotFolder { TransformationValidationException("Node <$it> isn't folder") }

            val transformedDataDescriptors =
                    validateAndTransform(transformerId, nodeDescriptors, targetMediaType, parameters, timeout)

            val targetNodeDescriptors =
                    targetFolderNodeRef.createNodesWithoutContent(transformedDataDescriptors.size,
                                                                  targetType ?: ContentModel.TYPE_CONTENT,
                                                                  targetContentProperty ?: ContentModel.PROP_CONTENT,
                                                                  determinedNamePattern)

            transformedDataDescriptors.saveInNodes(targetNodeDescriptors, targetMediaType)

            targetNodeDescriptors
        }

        logger.info("Transformed <{}> <{}, {}, {}> from nodes <{}> to folder <{}> <{}, {}, {}> in <{} s>",
                    transformerId,
                    targetMediaType,
                    parameters.getAll(),
                    timeout,
                    nodeDescriptors,
                    targetFolderNodeRef,
                    targetType,
                    targetContentProperty,
                    namePattern,
                    measuredTimeMs)

        return targetNodeDescriptors.map { it.nodeRef }
    }

    private fun validateAndTransform(transformerId: String,
                                     nodeDescriptors: List<NodeDescriptor>,
                                     targetMediaType: MediaType,
                                     parameters: Parameters,
                                     timeout: Long?): List<TransformedDataDescriptor> {
        nodeDescriptors.throwIfAnyNodeDoesNotExist { TransformationValidationException("Node <$it> doesn't exist") }

        val dataDescriptors = nodeDescriptors.toDataDescriptors()

        return transformationServerService.transform(transformerId,
                                                     dataDescriptors,
                                                     targetMediaType,
                                                     parameters,
                                                     timeout)
    }

    private fun List<NodeDescriptor>.throwIfAnyNodeDoesNotExist(toThrow: (NodeRef) -> Throwable) {
        this.forEach {
            it.nodeRef.throwIfNotExist(toThrow)
        }
    }

    private fun NodeRef.throwIfNotExist(toThrow: (NodeRef) -> Throwable) {
        if (!nodeService.exists(this)) {
            throw toThrow(this)
        }
    }

    private fun NodeRef.throwIfNotFolder(toThrow: (NodeRef) -> Throwable) {
        if (!dictionaryService.isSubClass(nodeService.getType(this), ContentModel.TYPE_FOLDER)) {
            throw toThrow(this)
        }
    }

    private fun List<NodeDescriptor>.toDataDescriptors(): List<DataDescriptor> =
            this.map {
                val contentReader = contentService.getReader(it.nodeRef, it.contentProperty)
                val mediaType = MediaType.create(contentReader.mimetype, Charset.forName(contentReader.encoding))

                DataDescriptor(alfrescoDataConverter.createData(contentReader), mediaType)
            }

    private fun NodeRef.createNodesWithoutContent(size: Int,
                                                  targetType: QName,
                                                  targetContentProperty: QName,
                                                  namePattern: String): List<NodeDescriptor> {
        return (0 until size).map { index ->
            val name = namePattern.replace("\${index}", index.toString())

            val targetNodeRef = fileFolderService.create(this,
                                                         name,
                                                         targetType)
                    .nodeRef

            NodeDescriptor(targetNodeRef,
                           targetContentProperty)
        }
    }

    private fun List<TransformedDataDescriptor>.saveInNodes(nodeDescriptors: List<NodeDescriptor>,
                                                            mediaType: MediaType) {
        this.zip(nodeDescriptors)
                .forEach { (dataDescriptor, nodeDescriptor) ->
                    dataDescriptor.saveInNode(nodeDescriptor.nodeRef, nodeDescriptor.contentProperty, mediaType)
                }
    }

    private fun TransformedDataDescriptor.saveInNode(nodeRef: NodeRef, contentProperty: QName, mediaType: MediaType) {
        val contentWriter = createContentWriter(nodeRef, contentProperty, mediaType)

        alfrescoDataConverter.saveDataInContentWriter(data, contentWriter)

        metadata.saveAlfrescoPropertiesInNode(nodeRef)
    }

    private fun createContentWriter(nodeRef: NodeRef,
                                    contentProperty: QName,
                                    mediaType: MediaType) =
            contentService.getWriter(nodeRef, contentProperty, true).apply {
                mimetype = mediaType.mimeType
                encoding = mediaType.charset.name()
            }

    private fun Metadata.saveAlfrescoPropertiesInNode(nodeRef: NodeRef) {
        val alfrescoProperties = this.getAlfrescoProperties()

        if (alfrescoProperties.isNotEmpty()) {
            alfrescoProperties.forEach {
                nodeService.setProperty(nodeRef, QName.createQName(it.key, namespaceService), it.value as Serializable?)
            }

            logger.debug("Saved properties <{}> in node <{}>", alfrescoProperties, nodeRef)
        }
    }

    private fun Metadata.getAlfrescoProperties(): Map<String, Any> =
            this.getAll()
                    .filter { it.key.startsWith("alf_") }
                    .map { it.key.removePrefix("alf_") to it.value }
                    .toMap()

    private fun validateNamePattern(namePattern: String) {
        if (!namePattern.contains("\${index}")) {
            throw TransformationValidationException("Name pattern <$namePattern> doesn't contain <\${index}>")
        }
    }

    private fun <T> measureTimeMillisWithResult(block: () -> T): Pair<Long, T> {
        val start = System.currentTimeMillis()
        val result = block()
        return System.currentTimeMillis() - start to result
    }

}