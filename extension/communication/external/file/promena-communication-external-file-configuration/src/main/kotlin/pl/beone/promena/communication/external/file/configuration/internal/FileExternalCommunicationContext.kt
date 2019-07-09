package pl.beone.promena.communication.external.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.external.file.internal.FileIncomingExternalCommunicationConverter
import pl.beone.promena.communication.external.file.internal.FileOutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

@Configuration
class FileExternalCommunicationContext {

    @Bean
    fun fileExternalCommunication(environment: Environment,
                                  fileIncomingExternalCommunicationConverter: FileIncomingExternalCommunicationConverter,
                                  fileOutgoingExternalCommunicationConverter: FileOutgoingExternalCommunicationConverter) =
            ExternalCommunication(environment.getCommunicationId(),
                                  fileIncomingExternalCommunicationConverter,
                                  fileOutgoingExternalCommunicationConverter)

    private fun Environment.getCommunicationId(): String =
            getRequiredProperty("communication.external.file.id")
}