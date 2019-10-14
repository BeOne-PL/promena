package pl.beone.promena.alfresco.module.connector.http.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.http.external.HttpClientPromenaTransformer
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.TransformedDataDescriptorSaver
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import reactor.netty.http.client.HttpClient

@Configuration
class HttpClientPromenaTransformerContext {

    @Bean
    fun httpClientPromenaTransformer(
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        retry: Retry,
        nodesChecksumGenerator: NodesChecksumGenerator,
        dataDescriptorGetter: DataDescriptorGetter,
        transformedDataDescriptorSaver: TransformedDataDescriptorSaver,
        authorizationService: AuthorizationService,
        kryoSerializationService: KryoSerializationService,
        httpClient: HttpClient
    ) =
        HttpClientPromenaTransformer(
            externalCommunicationParameters,
            retry,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformedDataDescriptorSaver,
            authorizationService,
            kryoSerializationService,
            httpClient
        )
}