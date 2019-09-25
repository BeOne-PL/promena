package pl.beone.promena.alfresco.module.core.configuration.external

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.MemoryOrFileAlfrescoDataConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

@Configuration
class MemoryOrFileAlfrescoDataConverterContext {

    @Bean
    fun memoryOrFileAlfrescoDataConverter(
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters
    ): MemoryOrFileAlfrescoDataConverter =
        MemoryOrFileAlfrescoDataConverter(externalCommunicationParameters.getId(), getDirectoryIfFileOrNull(externalCommunicationParameters))

    private fun getDirectoryIfFileOrNull(externalCommunicationParameters: CommunicationParameters): File? =
        if (externalCommunicationParameters.getId() == FileCommunicationParameters.ID) {
            (externalCommunicationParameters as FileCommunicationParameters).getDirectory()
        } else {
            null
        }
}
