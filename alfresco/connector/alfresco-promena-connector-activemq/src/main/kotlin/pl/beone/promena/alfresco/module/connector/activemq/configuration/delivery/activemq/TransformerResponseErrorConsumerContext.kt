package pl.beone.promena.alfresco.module.connector.activemq.configuration.delivery.activemq

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseErrorConsumer
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerResponseProcessor
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.ActiveMQPromenaTransformationExecutor
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager

@Configuration
class TransformerResponseErrorConsumerContext {

    @Bean
    fun transformerResponseErrorConsumer(
        promenaMutableTransformationManager: PromenaMutableTransformationManager,
        transformerResponseProcessor: TransformerResponseProcessor,
        activeMQPromenaTransformer: ActiveMQPromenaTransformationExecutor,
        authorizationService: AuthorizationService,
        transformationParametersSerializationService: TransformationParametersSerializationService
    ) =
        TransformerResponseErrorConsumer(
            promenaMutableTransformationManager,
            transformerResponseProcessor,
            activeMQPromenaTransformer,
            authorizationService,
            transformationParametersSerializationService
        )
}
