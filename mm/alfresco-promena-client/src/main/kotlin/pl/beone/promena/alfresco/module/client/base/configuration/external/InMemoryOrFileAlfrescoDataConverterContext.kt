package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.external.InMemoryOrFileAlfrescoDataConverter

@Configuration
class InMemoryOrFileAlfrescoDataConverterContext {

    @Bean
    fun inMemoryOrFileAlfrescoDataConverter(externalCommunication: ExternalCommunication): InMemoryOrFileAlfrescoDataConverter =
            InMemoryOrFileAlfrescoDataConverter(externalCommunication.id, externalCommunication.location)
}
