package pl.beone.promena.communication.file.internal.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.converter.FileInternalCommunicationConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(
        internalCommunicationParameters: FileCommunicationParameters
    ) =
        FileInternalCommunicationConverter(
            internalCommunicationParameters.getDirectory()
        )
}