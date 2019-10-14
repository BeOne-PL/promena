package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

class PromenaRenditionInProgressException(
    val nodeRef: NodeRef,
    val renditionName: String
) : IllegalStateException("Creating rendition <$renditionName> of <$nodeRef> is in progress...")