package pl.beone.promena.alfresco.module.rendition.predefined.configuration.definition.image

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.definition.image.MediumPromenaRenditionDefinition

@Configuration
class MediumPromenaRenditionDefinitionContext {

    @Bean
    fun mediumPromenaRenditionDefinition() =
        MediumPromenaRenditionDefinition
}