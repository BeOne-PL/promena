package pl.beone.promena.communication.internal.file.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.internal.file.internal.FileInternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(@Qualifier("internalCommunicationParameters") communicationParameters: CommunicationParameters) =
            FileInternalCommunicationConverter(communicationParameters)
}