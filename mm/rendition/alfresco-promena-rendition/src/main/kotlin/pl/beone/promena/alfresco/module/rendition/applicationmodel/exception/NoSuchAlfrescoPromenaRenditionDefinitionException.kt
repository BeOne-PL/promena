package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

class NoSuchAlfrescoPromenaRenditionDefinitionException(
    val renditionName: String,
    val alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
) : NoSuchElementException(
    "There is no <$renditionName> Promena rendition definition. Available renditions: <[${createDescription(alfrescoPromenaRenditionDefinitions)}]>"
)

private fun createDescription(alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>) =
    alfrescoPromenaRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${it.getRenditionName()})" }

