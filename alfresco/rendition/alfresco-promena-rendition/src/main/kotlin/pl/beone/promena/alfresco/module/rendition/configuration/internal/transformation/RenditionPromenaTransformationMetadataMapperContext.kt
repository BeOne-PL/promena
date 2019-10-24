package pl.beone.promena.alfresco.module.rendition.configuration.internal.transformation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.internal.transformation.RenditionPromenaTransformationMetadataMapper

@Configuration
class RenditionPromenaTransformationMetadataMapperContext {

    @Bean
    fun renditionPromenaTransformationMetadataMapper() =
        RenditionPromenaTransformationMetadataMapper()
}