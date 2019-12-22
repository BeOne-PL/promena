package pl.beone.promena.alfresco.lib.rendition.configuration.external.transformation

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.lib.rendition.external.transformation.RenditionPromenaTransformationMetadataSaver

@Configuration
class RenditionPromenaTransformationMetadataSaverContext {

    @Bean
    fun renditionPromenaTransformationMetadataSaver(
        serviceRegistry: ServiceRegistry
    ) =
        RenditionPromenaTransformationMetadataSaver(
            serviceRegistry
        )
}