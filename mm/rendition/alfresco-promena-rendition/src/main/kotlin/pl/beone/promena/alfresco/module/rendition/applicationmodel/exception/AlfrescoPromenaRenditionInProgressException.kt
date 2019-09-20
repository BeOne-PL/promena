package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

class AlfrescoPromenaRenditionInProgressException(
    val nodeRef: NodeRef,
    val renditionName: String
) : IllegalStateException("Rendition <$renditionName> of <$nodeRef> is in progress...")