package pl.beone.promena.alfresco.module.rendition.configuration.internal.transformation.definition

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.internal.transformation.definition.PromenaRenditionNamePromenaTransformationMetadataMappingDefinition

@Configuration
class PromenaRenditionNamePromenaTransformationMetadataMappingDefinitionContext {

    @Bean
    fun promenaRenditionNamePromenaTransformationMetadataMappingDefinition() =
        PromenaRenditionNamePromenaTransformationMetadataMappingDefinition()
}