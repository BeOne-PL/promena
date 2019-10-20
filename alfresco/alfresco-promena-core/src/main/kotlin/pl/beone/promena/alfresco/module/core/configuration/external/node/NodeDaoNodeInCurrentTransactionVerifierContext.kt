package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.alfresco.repo.domain.node.NodeDAO
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.node.NodeDaoNodeInCurrentTransactionVerifier

@Configuration
class NodeDaoNodeInCurrentTransactionVerifierContext {

    @Bean
    fun nodeDaoNodeInCurrentTransactionVerifier(
        nodeDAO: NodeDAO
    ) =
        NodeDaoNodeInCurrentTransactionVerifier(
            nodeDAO
        )
}
