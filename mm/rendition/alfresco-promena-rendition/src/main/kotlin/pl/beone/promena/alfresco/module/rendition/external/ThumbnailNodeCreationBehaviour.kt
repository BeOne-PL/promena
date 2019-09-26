package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import org.alfresco.model.ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA
import org.alfresco.model.ContentModel.TYPE_THUMBNAIL
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy
import org.alfresco.repo.policy.Behaviour.NotificationFrequency.TRANSACTION_COMMIT
import org.alfresco.repo.policy.JavaBehaviour
import org.alfresco.repo.policy.PolicyComponent
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import java.io.Serializable
import javax.annotation.PostConstruct


class ThumbnailNodeCreationBehaviour(
    private val policyComponent: PolicyComponent,
    private val nodeService: NodeService
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    fun register() {
        policyComponent.bindClassBehaviour(
            OnCreateNodePolicy.QNAME,
            TYPE_THUMBNAIL,
            JavaBehaviour(this, "onCreateNode", TRANSACTION_COMMIT)
        )
    }

    fun onCreateNode(childAssociationRef: ChildAssociationRef) {
        val nodeRef = childAssociationRef.childRef
        if (nodeRef != null && nodeService.exists(nodeRef)) {
            val renditionName = nodeService.getProperty(nodeRef, PROP_RENDITION_NAME) as String?
            if (renditionName != null) {
                val parentNodeRef = childAssociationRef.parentRef
                val lastThumbnailModificationData = determineLastThumbnailModificationData(parentNodeRef, renditionName) as Serializable
                nodeService.setProperty(parentNodeRef, PROP_LAST_THUMBNAIL_MODIFICATION_DATA, lastThumbnailModificationData)
                logger.debug { "Set <cm:${PROP_LAST_THUMBNAIL_MODIFICATION_DATA.localName}> to <$lastThumbnailModificationData> in <$nodeRef>" }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun determineLastThumbnailModificationData(nodeRef: NodeRef, renditionName: String): List<String> =
        ((nodeService.getProperty(nodeRef, PROP_LAST_THUMBNAIL_MODIFICATION_DATA) ?: listOf<String>()) as List<String>)
            .filterNot { it.startsWith(renditionName) } +
                createLastThumbnailModificationDataItem(renditionName)

    private fun createLastThumbnailModificationDataItem(renditionName: String): String =
        renditionName + ":" + System.currentTimeMillis()
}