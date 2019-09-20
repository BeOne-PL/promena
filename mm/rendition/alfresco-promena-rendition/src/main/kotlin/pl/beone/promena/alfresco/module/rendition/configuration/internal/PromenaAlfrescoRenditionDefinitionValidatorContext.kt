package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.internal.PromenaAlfrescoRenditionDefinitionValidator

@Configuration
class PromenaAlfrescoRenditionDefinitionValidatorContext {

    @Bean
    fun promenaAlfrescoRenditionDefinitionValidator(
        promenaAlfrescoRenditionDefinitionGetter: PromenaAlfrescoRenditionDefinitionGetter
    ) =
        PromenaAlfrescoRenditionDefinitionValidator(
            promenaAlfrescoRenditionDefinitionGetter.getAll()
        )
}