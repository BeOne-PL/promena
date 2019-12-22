package pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution

class PromenaRenditionInProgressException(
    val nodeRef: NodeRef,
    val renditionName: String,
    val transformationExecution: TransformationExecution
) : IllegalStateException("Creating rendition <$renditionName> of <$nodeRef> is in progress in transformation <${transformationExecution.id}>...")