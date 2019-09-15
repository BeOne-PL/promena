package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition

class PromenaNoSuchRenditionDefinitionException(
    message: String,
    val promenaAlfrescoRenditionDefinitions: List<PromenaAlfrescoRenditionDefinition>
) : NoSuchElementException(message)