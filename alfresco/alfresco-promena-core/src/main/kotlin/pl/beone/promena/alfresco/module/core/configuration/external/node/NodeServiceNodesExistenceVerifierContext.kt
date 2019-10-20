package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.node.NodeServiceNodesExistenceVerifier

@Configuration
class NodeServiceNodesExistenceVerifierContext {

    @Bean
    fun nodeServiceNodesExistenceVerifier(
        serviceRegistry: ServiceRegistry
    ) =
        NodeServiceNodesExistenceVerifier(
            serviceRegistry
        )
}
