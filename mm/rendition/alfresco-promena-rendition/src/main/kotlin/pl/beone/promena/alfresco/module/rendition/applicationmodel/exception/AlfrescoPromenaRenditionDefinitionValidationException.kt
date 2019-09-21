package pl.beone.promena.alfresco.module.rendition.applicationmodel.exception

import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

class AlfrescoPromenaRenditionDefinitionValidationException(
    val renditionNameToNotUniqueDefinitionsMap: Map<String, List<AlfrescoPromenaRenditionDefinition>>
) : IllegalStateException(
    "Detected <${renditionNameToNotUniqueDefinitionsMap.size}> definitions with duplicated rendition name:\n" +
            createDescription(renditionNameToNotUniqueDefinitionsMap)
)

private fun createDescription(renditionNameToNotUniqueDefinitionsMap: Map<String, List<AlfrescoPromenaRenditionDefinition>>): String {
    return renditionNameToNotUniqueDefinitionsMap.entries.joinToString("\n") { (renditionName, definitions) ->
        "> $renditionName: <${definitions.joinToString(", ") { it::class.java.canonicalName }}>"
    }
}