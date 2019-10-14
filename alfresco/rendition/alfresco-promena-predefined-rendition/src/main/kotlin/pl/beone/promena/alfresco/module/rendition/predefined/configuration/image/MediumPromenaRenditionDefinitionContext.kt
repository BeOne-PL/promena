package pl.beone.promena.alfresco.module.rendition.predefined.configuration.image

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.image.MediumPromenaRenditionDefinition

@Configuration
class MediumPromenaRenditionDefinitionContext {

    @Bean
    fun mediumPromenaRenditionDefinition() =
        MediumPromenaRenditionDefinition
}