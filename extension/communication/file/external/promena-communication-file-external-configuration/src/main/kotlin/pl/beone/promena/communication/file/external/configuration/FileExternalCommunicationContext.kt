package pl.beone.promena.communication.file.external.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.external.configuration.extension.getId
import pl.beone.promena.communication.file.external.internal.converter.FileIncomingExternalCommunicationConverter
import pl.beone.promena.communication.file.external.internal.converter.FileOutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

@Configuration
class FileExternalCommunicationContext {

    @Bean
    fun fileExternalCommunication(
        environment: Environment,
        fileIncomingExternalCommunicationConverter: FileIncomingExternalCommunicationConverter,
        fileOutgoingExternalCommunicationConverter: FileOutgoingExternalCommunicationConverter
    ) =
        ExternalCommunication(
            environment.getId(),
            fileIncomingExternalCommunicationConverter,
            fileOutgoingExternalCommunicationConverter
        )
}