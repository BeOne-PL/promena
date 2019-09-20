package pl.beone.promena.alfresco.module.rendition.internal

import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaAlfrescoRenditionValidationException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import javax.annotation.PostConstruct

class AlfrescoPromenaRenditionDefinitionValidator(
    private val alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
) {

    @PostConstruct
    fun validateUniqueDefinitions() {
        val notUniqueDefinitions = alfrescoPromenaRenditionDefinitions.groupBy { it.getRenditionName() }
            .filter { (_, definitions) -> definitions.size >= 2 }
            .toList()

        if (notUniqueDefinitions.isNotEmpty()) {
            throw PromenaAlfrescoRenditionValidationException(
                "Detected <${notUniqueDefinitions.size}> definitions with duplicated rendition name:\n" +
                        notUniqueDefinitions.joinToString("\n") { (renditionName, definitions) ->
                            "> $renditionName: <${definitions.joinToString(", ") { it::class.java.canonicalName }}>"
                        }
            )
        }
    }
}