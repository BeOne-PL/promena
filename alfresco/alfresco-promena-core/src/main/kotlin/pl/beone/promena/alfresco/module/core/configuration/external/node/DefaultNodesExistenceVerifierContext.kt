package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.node.DefaultNodesExistenceVerifier

@Configuration
class DefaultNodesExistenceVerifierContext {

    @Bean
    fun defaultNodesExistenceVerifier(
        serviceRegistry: ServiceRegistry
    ) =
        DefaultNodesExistenceVerifier(
            serviceRegistry
        )
}
