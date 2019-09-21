package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.stereotype.Component
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionDefinitionValidationException
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

        if (renditionNameToNotUniqueDefinitionsMap.isNotEmpty()) {
            throw AlfrescoPromenaRenditionDefinitionValidationException(renditionNameToNotUniqueDefinitionsMap)
        }
    }
}