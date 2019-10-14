package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(
        nodesChecksumGenerator: NodesChecksumGenerator,
        activeMQPromenaTransformer: ActiveMQPromenaTransformer,
        authorizationService: AuthorizationService,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformationParametersSerializationService: TransformationParametersSerializationService
    ): TransformerResponseErrorConsumer =
        TransformerResponseErrorConsumer(
            nodesChecksumGenerator,
            authorizationService,
            reactiveTransformationManager,
            activeMQPromenaTransformer,
            transformationParametersSerializationService
        )
}
