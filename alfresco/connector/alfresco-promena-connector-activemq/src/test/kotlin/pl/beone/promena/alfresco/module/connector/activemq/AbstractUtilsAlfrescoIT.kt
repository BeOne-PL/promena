package pl.beone.promena.alfresco.module.connector.activemq

import org.alfresco.model.ContentModel.TYPE_CONTENT
import org.alfresco.model.ContentModel.TYPE_FOLDER
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.service.cmr.model.FileExistsException
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import java.io.Serializable
import java.time.LocalDateTime

abstract class AbstractUtilsAlfrescoIT : AbstractAlfrescoIT() {

    protected fun NodeRef.createNode(targetType: QName = TYPE_CONTENT, name: String? = null): NodeRef {
        val determinedNamePattern = name ?: LocalDateTime.now().toString().replace(":", "_")

        return serviceRegistry.fileFolderService.create(this, determinedNamePattern, targetType).nodeRef
    }

    protected fun NodeRef.getProperty(qname: QName): Serializable =
        serviceRegistry.nodeService.getProperty(this, qname)

    protected fun createOrGetIntegrationTestsFolder(): NodeRef =
        try {
            serviceRegistry.fileFolderService.create(getCompanyHomeNodeRef(), "Integration test", TYPE_FOLDER)
                .nodeRef
        } catch (e: FileExistsException) {
            serviceRegistry.fileFolderService.searchSimple(getCompanyHomeNodeRef(), "Integration test")
        }

    private fun getCompanyHomeNodeRef(): NodeRef =
        serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)
}