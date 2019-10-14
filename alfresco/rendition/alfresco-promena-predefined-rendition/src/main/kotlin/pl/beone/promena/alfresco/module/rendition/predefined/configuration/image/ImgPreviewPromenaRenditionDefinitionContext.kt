package pl.beone.promena.alfresco.module.rendition.predefined.configuration.image

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.predefined.internal.image.ImgPreviewPromenaRenditionDefinition

@Configuration
class ImgPreviewPromenaRenditionDefinitionContext {

    @Bean
    fun imgPreviewPromenaRenditionDefinition() =
        ImgPreviewPromenaRenditionDefinition
}