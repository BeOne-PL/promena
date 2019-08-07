package pl.beone.promena.communication.file.external.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.external.internal.FileIncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class FileIncomingExternalCommunicationConverterContext {

    @Bean
    fun fileIncomingExternalCommunicationConverter(
        @Qualifier("fileExternalCommunication") externalCommunication: ExternalCommunication,
        @Qualifier("internalCommunicationParameters") internalCommunicationParameters: CommunicationParameters,
        internalCommunicationConverter: InternalCommunicationConverter
    ) =
        FileIncomingExternalCommunicationConverter(
            externalCommunication.id,
            internalCommunicationParameters,
            internalCommunicationConverter
        )
}