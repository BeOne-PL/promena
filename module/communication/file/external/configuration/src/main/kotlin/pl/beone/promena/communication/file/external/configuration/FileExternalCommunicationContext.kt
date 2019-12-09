package pl.beone.promena.communication.file.external.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.external.internal.converter.FileIncomingExternalCommunicationConverter
import pl.beone.promena.communication.file.external.internal.converter.FileOutgoingExternalCommunicationConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

@Configuration
class FileExternalCommunicationContext {

    @Bean
    fun fileExternalCommunication(
        fileIncomingExternalCommunicationConverter: FileIncomingExternalCommunicationConverter,
        fileOutgoingExternalCommunicationConverter: FileOutgoingExternalCommunicationConverter
    ) =
        ExternalCommunication(
            FileCommunicationParametersConstants.ID,
            fileIncomingExternalCommunicationConverter,
            fileOutgoingExternalCommunicationConverter
        )
}