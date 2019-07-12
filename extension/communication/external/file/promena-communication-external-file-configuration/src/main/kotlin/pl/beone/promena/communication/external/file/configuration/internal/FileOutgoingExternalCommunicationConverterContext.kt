package pl.beone.promena.communication.external.file.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.file.internal.FileOutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class FileOutgoingExternalCommunicationConverterContext {

    @Bean
    fun fileOutgoingExternalCommunicationConverter(@Qualifier("internalCommunicationParameters") communicationParameters: CommunicationParameters) =
            FileOutgoingExternalCommunicationConverter(communicationParameters)
}