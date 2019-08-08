package pl.beone.promena.communication.file.external.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.external.internal.FileIncomingExternalCommunicationConverter
import pl.beone.promena.communication.file.external.internal.FileOutgoingExternalCommunicationConverter
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