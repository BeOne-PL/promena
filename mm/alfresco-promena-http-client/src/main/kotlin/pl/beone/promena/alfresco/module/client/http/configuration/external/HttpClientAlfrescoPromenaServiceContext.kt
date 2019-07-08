package pl.beone.promena.alfresco.module.client.http.configuration.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.http.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.alfresco.module.client.http.configuration.toDuration
import pl.beone.promena.alfresco.module.client.http.external.HttpClientAlfrescoPromenaService
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import reactor.netty.http.client.HttpClient
import java.util.*

@Configuration
class HttpClientAlfrescoPromenaServiceContext {

    companion object {
        private val logger = LoggerFactory.getLogger(HttpClientAlfrescoPromenaServiceContext::class.java)
    }

    @Bean
    fun httpClientAlfrescoPromenaService(@Qualifier("global-properties") properties: Properties,
                                         externalCommunication: ExternalCommunication,
                                         alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                         alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                         alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                                         kryoSerializationService: KryoSerializationService,
                                         httpClient: HttpClient) =
            HttpClientAlfrescoPromenaService(externalCommunication,
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.enabled").toBoolean(),
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.max-attempts").toLong(),
                                             properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.transformation.error.retry.next-attempt-delay").toDuration(),
                                             alfrescoNodesChecksumGenerator,
                                             alfrescoDataDescriptorGetter,
                                             alfrescoTransformedDataDescriptorSaver,
                                             kryoSerializationService,
                                             httpClient)
}