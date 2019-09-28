package pl.beone.promena.alfresco.module.connector.http.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.http.external.HttpClientAlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import reactor.netty.http.client.HttpClient

@Configuration
class HttpClientAlfrescoPromenaTransformerContext {

    @Bean
    fun httpClientAlfrescoPromenaTransformer(
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters,
        retry: Retry,
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
        alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
        alfrescoAuthenticationService: AlfrescoAuthenticationService,
        kryoSerializationService: KryoSerializationService,
        httpClient: HttpClient
    ) =
        HttpClientAlfrescoPromenaTransformer(
            externalCommunicationParameters,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            alfrescoTransformedDataDescriptorSaver,
            alfrescoAuthenticationService,
            kryoSerializationService,
            httpClient
        )
}