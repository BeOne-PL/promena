package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseProcessor
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.node.NodesExistenceVerifier
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager

@Configuration
class TransformerResponseProcessorContext {

    @Bean
    fun transformerResponseProcessor(
        promenaMutableTransformationManager: PromenaMutableTransformationManager,
        nodesExistenceVerifier: NodesExistenceVerifier,
        nodesChecksumGenerator: NodesChecksumGenerator,
        authorizationService: AuthorizationService
    ) =
        TransformerResponseProcessor(
            promenaMutableTransformationManager,
            nodesExistenceVerifier,
            nodesChecksumGenerator,
            authorizationService
        )
}