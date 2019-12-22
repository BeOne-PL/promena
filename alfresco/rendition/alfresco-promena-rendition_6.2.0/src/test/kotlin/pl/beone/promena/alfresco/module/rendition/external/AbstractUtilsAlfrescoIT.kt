package pl.beone.promena.alfresco.module.rendition.external

import org.alfresco.model.ContentModel.*
import org.alfresco.model.RenditionModel.ASSOC_RENDITION
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.service.cmr.model.FileExistsException
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaModel.PROPERTY_RENDITION_NAME
import java.time.LocalDateTime

abstract class AbstractUtilsAlfrescoIT : AbstractAlfrescoIT() {

    protected fun NodeRef.createNode(targetType: QName = TYPE_CONTENT, name: String? = null): NodeRef {
        val determinedNamePattern = name ?: createNameBasedOnDate()

        return serviceRegistry.fileFolderService.create(this, determinedNamePattern, targetType).nodeRef
    }

    protected fun NodeRef.createRenditionNode(): ChildAssociationRef =
        serviceRegistry.nodeService.createNode(
            this,
            ASSOC_RENDITION,
            QName.createQName(CONTENT_MODEL_1_0_URI, createNameBasedOnDate()),
            TYPE_THUMBNAIL,
            emptyMap()
        )

    protected fun ChildAssociationRef.setRenditionName(renditionName: String): ChildAssociationRef =
        this.also { serviceRegistry.nodeService.setProperty(this.childRef, PROPERTY_RENDITION_NAME, renditionName) }

    protected fun createOrGetIntegrationTestsFolder(): NodeRef =
        try {
            serviceRegistry.fileFolderService.create(getCompanyHomeNodeRef(), "Integration test", TYPE_FOLDER)
                .nodeRef
        } catch (e: FileExistsException) {
            serviceRegistry.fileFolderService.searchSimple(getCompanyHomeNodeRef(), "Integration test")
        }

    private fun getCompanyHomeNodeRef(): NodeRef =
        serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)

    private fun createNameBasedOnDate(): String =
        LocalDateTime.now().toString().replace(":", "_")
}