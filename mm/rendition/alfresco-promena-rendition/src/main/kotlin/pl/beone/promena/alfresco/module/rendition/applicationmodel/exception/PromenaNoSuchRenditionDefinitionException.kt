package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

class PromenaNoSuchRenditionDefinitionException(
    val renditionName: String,
    val alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
) : NoSuchElementException(
    "Definition for <$renditionName> rendition isn't available. " +
            "Available renditions: <[${createDescription(alfrescoPromenaRenditionDefinitions)}]>"
)

private fun createDescription(alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>) =
    alfrescoPromenaRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${it.getRenditionName()})" }

