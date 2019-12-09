package pl.beone.promena.alfresco.module.core.configuration.external.node

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.node.MemoryOrFileDataConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File

@Configuration
class MemoryOrFileDataConverterContext {

    @Bean
    fun memoryOrFileDataConverter(
        @Qualifier("externalCommunicationParameters") externalCommunicationParameters: CommunicationParameters
    ): MemoryOrFileDataConverter =
        MemoryOrFileDataConverter(
            externalCommunicationParameters.getId(),
            getDirectoryIfFileOrNull(externalCommunicationParameters)
        )

    private fun getDirectoryIfFileOrNull(externalCommunicationParameters: CommunicationParameters): File? =
        if (externalCommunicationParameters.getId() == FileCommunicationParametersConstants.ID) {
            externalCommunicationParameters.getDirectory()
        } else {
            null
        }
}
