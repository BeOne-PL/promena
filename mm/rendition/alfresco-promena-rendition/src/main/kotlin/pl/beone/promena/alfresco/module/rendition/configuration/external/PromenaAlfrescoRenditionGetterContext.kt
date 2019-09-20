package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.external.PromenaAlfrescoRenditionGetter

@Configuration
class PromenaAlfrescoRenditionGetterContext {

    @Bean
    fun promenaAlfrescoRenditionGetter(
        nodeService: NodeService
    ) =
        PromenaAlfrescoRenditionGetter(
            nodeService
        )
}