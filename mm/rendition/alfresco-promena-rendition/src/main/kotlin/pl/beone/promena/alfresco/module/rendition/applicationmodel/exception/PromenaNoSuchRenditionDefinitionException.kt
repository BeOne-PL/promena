package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

class PromenaNoSuchRenditionDefinitionException(
    message: String,
    val alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
) : NoSuchElementException(message)