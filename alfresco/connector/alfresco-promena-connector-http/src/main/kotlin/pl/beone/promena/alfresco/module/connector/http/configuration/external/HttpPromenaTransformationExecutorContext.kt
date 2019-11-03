package pl.beone.promena.alfresco.module.connector.http.configuration.external

import org.alfresco.service.ServiceRegistry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.http.external.HttpPromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.*
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorInjector
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.util.*

@Configuration
class HttpPromenaTransformationExecutorContext {

    @Bean
    fun httpPromenaTransformationExecutor(
        @Qualifier("global-properties") properties: Properties,
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        promenaMutableTransformationManager: PromenaTransformationManager.PromenaMutableTransformationManager,
        retry: Retry,
        postTransformationExecutorValidator: PostTransformationExecutorValidator,
        nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier,
        nodesChecksumGenerator: NodesChecksumGenerator,
        nodesExistenceVerifier: NodesExistenceVerifier,
        dataDescriptorGetter: DataDescriptorGetter,
        transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
        postTransformationExecutorInjector: PostTransformationExecutorInjector,
        authorizationService: AuthorizationService,
        serviceRegistry: ServiceRegistry,
        kryoSerializationService: KryoSerializationService
    ) =
        HttpPromenaTransformationExecutor(
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.execution.threads").toInt(),
            properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.host") +
                    ":" +
                    properties.getRequiredPropertyWithResolvedPlaceholders("promena.connector.http.port"),
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            retry,
            postTransformationExecutorValidator,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            nodesExistenceVerifier,
            dataDescriptorGetter,
            transformedDataDescriptorSaver,
            postTransformationExecutorInjector,
            authorizationService,
            serviceRegistry,
            kryoSerializationService
        )
}