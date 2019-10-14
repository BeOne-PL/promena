package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinition

class NoSuchPromenaRenditionDefinitionException(
    val renditionName: String,
    val promenaRenditionDefinitions: List<PromenaRenditionDefinition>
) : NoSuchElementException(
    "There is no <$renditionName> Promena rendition definition. Available renditions: <[${createDescription(promenaRenditionDefinitions)}]>"
)

private fun createDescription(promenaRenditionDefinitions: List<PromenaRenditionDefinition>) =
    promenaRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${it.getRenditionName()})" }

