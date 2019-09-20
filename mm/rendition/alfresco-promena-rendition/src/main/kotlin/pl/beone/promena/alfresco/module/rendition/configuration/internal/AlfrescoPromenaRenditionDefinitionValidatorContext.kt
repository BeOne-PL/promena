package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.internal.AlfrescoPromenaRenditionDefinitionValidator

@Configuration
class AlfrescoPromenaRenditionDefinitionValidatorContext {

    @Bean
    fun alfrescoPromenaRenditionDefinitionValidator(
        alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter
    ) =
        AlfrescoPromenaRenditionDefinitionValidator(
            alfrescoPromenaRenditionDefinitionGetter.getAll()
        )
}