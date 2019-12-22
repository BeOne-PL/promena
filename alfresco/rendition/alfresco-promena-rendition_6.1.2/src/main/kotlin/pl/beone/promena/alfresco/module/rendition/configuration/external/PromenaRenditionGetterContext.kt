package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionGetter

@Configuration
class PromenaRenditionGetterContext {

    @Bean
    fun promenaRenditionGetter(
        nodeService: NodeService
    ) =
        PromenaRenditionGetter(
            nodeService
        )
}