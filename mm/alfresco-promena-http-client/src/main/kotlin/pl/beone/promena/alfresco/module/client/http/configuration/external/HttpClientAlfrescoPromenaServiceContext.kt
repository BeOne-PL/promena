package pl.beone.promena.alfresco.module.client.http.configuration.external

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.http.external.HttpClientAlfrescoPromenaService
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import reactor.netty.http.client.HttpClient

@Configuration
class HttpClientAlfrescoPromenaServiceContext {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpClientAlfrescoPromenaServiceContext::class.java)
    }

    @Bean
    fun httpClientAlfrescoPromenaService(
        externalCommunication: ExternalCommunication,
        retry: Retry,
        alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
        alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
        alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
        kryoSerializationService: KryoSerializationService,
        httpClient: HttpClient
    ) =
        HttpClientAlfrescoPromenaService(
            externalCommunication,
            retry,
            alfrescoNodesChecksumGenerator,
            alfrescoDataDescriptorGetter,
            alfrescoTransformedDataDescriptorSaver,
            kryoSerializationService,
            httpClient
        )
}