package pl.beone.promena.module.http.client.external.alfresco

import org.alfresco.model.ContentModel
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.service.cmr.model.FileExistsException
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.ContentWriter
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import java.io.Serializable
import java.time.LocalDateTime

abstract class AbstractUtilsAlfrescoIT : AbstractAlfrescoIT() {

    protected fun NodeRef.createNode(targetType: QName = ContentModel.TYPE_CONTENT,
                                     name: String? = null): NodeRef {
        val determinedNamePattern = name ?: LocalDateTime.now().toString().replace(":", "_")

        return serviceRegistry.fileFolderService.create(this,
                                                        determinedNamePattern,
                                                        targetType).nodeRef
    }

    protected fun NodeRef.createNodes(size: Int,
                                      targetType: QName = ContentModel.TYPE_CONTENT,
                                      namePattern: String? = null): List<NodeRef> {
        val determinedNamePattern = namePattern ?: "${LocalDateTime.now().toString().replace(":", "_")} - \${index}"

        return (0 until size).map {
            serviceRegistry.fileFolderService.create(this,
                                                     determinedNamePattern.replace("\${index}", it.toString()),
                                                     targetType).nodeRef
        }
    }

    protected fun NodeRef.createFolder(): NodeRef =
            serviceRegistry.fileFolderService.create(this,
                                                     LocalDateTime.now().toString(),
                                                     ContentModel.TYPE_FOLDER).nodeRef

    protected fun NodeRef.getNodeRefs(): List<NodeRef> =
            serviceRegistry.fileFolderService.list(this)
                    .map { it.nodeRef }

    protected fun NodeRef.delete() {
        try {
            serviceRegistry.nodeService.deleteNode(this)
        } catch (e: InvalidNodeRefException) {
            // deliberately omit
        }
    }

    protected fun NodeRef.getType(): QName =
            serviceRegistry.nodeService.getType(this)

    protected fun NodeRef.getContentWriter(contentProperty: QName = ContentModel.PROP_CONTENT): ContentWriter =
            serviceRegistry.contentService.getWriter(this, contentProperty, true)

    protected fun NodeRef.saveContent(mediaType: MediaType,
                                      content: String,
                                      contentProperty: QName = ContentModel.PROP_CONTENT) =
            serviceRegistry.contentService.getWriter(this, contentProperty, true).apply {
                mimetype = mediaType.mimeType
                encoding = mediaType.charset.name()
            }.putContent(content)

    protected fun NodeRef.getContentReader(contentProperty: QName = ContentModel.PROP_CONTENT): ContentReader =
            serviceRegistry.contentService.getReader(this, contentProperty)

    protected fun NodeRef.readContent(contentProperty: QName = ContentModel.PROP_CONTENT): ByteArray =
            serviceRegistry.contentService.getReader(this, contentProperty)
                    .contentInputStream.readBytes()

    protected fun NodeRef.getProperties(): Map<QName, Serializable> =
            serviceRegistry.nodeService.getProperties(this)

    protected fun NodeRef.getProperty(qname: QName): Serializable =
            serviceRegistry.nodeService.getProperty(this, qname)

    protected fun NodeRef.getMimeType(contentProperty: QName = ContentModel.PROP_CONTENT): String =
            serviceRegistry.contentService.getReader(this, contentProperty)
                    .mimetype

    protected fun createOrGetIntegrationTestsFolder(): NodeRef =
            try {
                serviceRegistry.fileFolderService.create(getCompanyHomeNodeRef(),
                                                         "Integration test",
                                                         ContentModel.TYPE_FOLDER)
                        .nodeRef
            } catch (e: FileExistsException) {
                serviceRegistry.fileFolderService.searchSimple(getCompanyHomeNodeRef(), "Integration test")
            }

    protected fun getCompanyHomeNodeRef(): NodeRef =
            serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)
}