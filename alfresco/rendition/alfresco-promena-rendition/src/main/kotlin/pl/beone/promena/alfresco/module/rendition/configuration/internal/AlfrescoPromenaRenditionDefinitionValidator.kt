package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.stereotype.Component
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import javax.annotation.PostConstruct

@Component
class AlfrescoPromenaRenditionDefinitionValidator(
    private val alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter
) {

    @PostConstruct
    fun validateUniqueDefinitions() {
        val renditionNameToNotUniqueDefinitionsMap = alfrescoPromenaRenditionDefinitionGetter.getAll()
            .groupBy { it.getRenditionName() }
            .filter { (_, definitions) -> definitions.size >= 2 }
            .toMap()

        check(renditionNameToNotUniqueDefinitionsMap.isEmpty()) {
            "Detected <${renditionNameToNotUniqueDefinitionsMap.size}> definitions with duplicated rendition name:\n" +
                    renditionNameToNotUniqueDefinitionsMap.entries.joinToString("\n") { (renditionName, definitions) ->
                        "> $renditionName: <${definitions.joinToString(", ") { it::class.java.canonicalName }}>"
                    }
        }
    }
}