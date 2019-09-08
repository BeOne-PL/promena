package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.external.MemoryOrFileAlfrescoDataConverter

@Configuration
class MemoryOrFileAlfrescoDataConverterContext {

    @Bean
    fun memoryOrFileAlfrescoDataConverter(
        externalCommunication: ExternalCommunication
    ): MemoryOrFileAlfrescoDataConverter =
        MemoryOrFileAlfrescoDataConverter(externalCommunication.id, externalCommunication.directory)
}
