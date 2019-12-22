package pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition

class NoSuchPromenaRenditionDefinitionException(
    val renditionName: String,
    val promenaRenditionDefinitions: List<PromenaRenditionDefinition>
) : NoSuchElementException(
    "There is no <$renditionName> Promena rendition definition. Available renditions: <[${createDescription(promenaRenditionDefinitions)}]>"
)

private fun createDescription(promenaRenditionDefinitions: List<PromenaRenditionDefinition>) =
    promenaRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${it.getRenditionName()})" }

