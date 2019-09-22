package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.alfresco.repo.policy.PolicyComponent
import org.alfresco.service.cmr.repository.NodeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.external.ThumbnailModificationBehaviour

@Configuration
class ThumbnailModificationBehaviourContext {

    @Bean
    fun thumbnailModificationBehaviour(
        policyComponent: PolicyComponent,
        nodeService: NodeService
    ) =
        ThumbnailModificationBehaviour(
            policyComponent,
            nodeService
        )
}