package pl.beone.promena.communication.file.internal.configuration.internal

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.FileInternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.net.URI

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(
        @Qualifier("internalCommunicationParameters") communicationParameters: CommunicationParameters

    ) =
        FileInternalCommunicationConverter(
            communicationParameters.get("location", URI::class.java)
        )
}