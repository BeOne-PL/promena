package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionDefinitionManager
import pl.beone.promena.alfresco.module.rendition.internal.PromenaAlfrescoRenditionDefinitionValidator

@Configuration
class PromenaAlfrescoRenditionDefinitionValidatorContext {

    @Bean
    fun promenaAlfrescoRenditionDefinitionValidator(
        promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager
    ) =
        PromenaAlfrescoRenditionDefinitionValidator(
            promenaRenditionDefinitionManager.getAll()
        )
}