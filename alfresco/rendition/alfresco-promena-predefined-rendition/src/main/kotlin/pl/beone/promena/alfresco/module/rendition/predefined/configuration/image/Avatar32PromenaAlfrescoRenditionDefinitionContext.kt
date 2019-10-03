package pl.beone.promena.alfresco.module.rendition.predefined.configuration.image

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.image.Avatar32PromenaAlfrescoRenditionDefinition

@Configuration
class Avatar32PromenaAlfrescoRenditionDefinitionContext {

    @Bean
    fun avatar32PromenaAlfrescoRenditionDefinition() =
        Avatar32PromenaAlfrescoRenditionDefinition
}