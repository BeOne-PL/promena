package pl.beone.promena.alfresco.module.connector.activemq.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.connector.activemq.external.ActiveMQPromenaTransformer
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.util.*

@Configuration
class ActiveMQPromenaTransformerContext {

    @Bean
    fun activeMQPromenaTransformer(
        @Qualifier("global-properties") properties: Properties,
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        retry: Retry,
        nodesChecksumGenerator: NodesChecksumGenerator,
        dataDescriptorGetter: DataDescriptorGetter,
        reactiveTransformationManager: ReactiveTransformationManager,
        transformerSender: TransformerSender
    ) =
        ActiveMQPromenaTransformer(
            externalCommunicationParameters,
            retry,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            reactiveTransformationManager,
            transformerSender
        )
}