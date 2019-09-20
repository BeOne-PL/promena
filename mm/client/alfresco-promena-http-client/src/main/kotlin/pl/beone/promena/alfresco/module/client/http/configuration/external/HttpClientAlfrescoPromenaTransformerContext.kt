package pl.beone.promena.alfresco.module.client.http.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.http.external.HttpClientAlfrescoPromenaTransformer
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
        kryoSerializationService: KryoSerializationService,
        alfrescoAuthenticationService: AlfrescoAuthenticationService,
        httpClient: HttpClient
    ) =
        HttpClientAlfrescoPromenaTransformer(
            externalCommunicationParameters,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            alfrescoTransformedDataDescriptorSaver,
            kryoSerializationService,
            alfrescoAuthenticationService,
            httpClient
        )
}