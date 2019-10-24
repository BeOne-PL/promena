package pl.beone.promena.alfresco.module.connector.activemq.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.ActiveMQPromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.util.*

@Configuration
class ActiveMQPromenaTransformerContext {

    @Bean
    fun activeMQPromenaTransformer(
        @Qualifier("global-properties") properties: Properties,
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        promenaMutableTransformationManager: PromenaMutableTransformationManager,
        retry: Retry,
        postTransformationExecutorValidator: PostTransformationExecutorValidator,
        nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier,
        nodesChecksumGenerator: NodesChecksumGenerator,
        dataDescriptorGetter: DataDescriptorGetter,
        transformerSender: TransformerSender,
        authorizationService: AuthorizationService
    ) =
        ActiveMQPromenaTransformationExecutor(
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            retry,
            postTransformationExecutorValidator,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformerSender,
            authorizationService
        )
}