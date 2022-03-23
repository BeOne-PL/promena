package pl.beone.promena.communication.file.internal.configuration.internal.cleaner

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.cleaner.FileInternalCommunicationCleaner
import pl.beone.promena.communication.file.model.internal.getIsAlfdataMounted
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class FileInternalCommunicationCleanerContext {

    @Bean
    fun fileInternalCommunicationCleaner(internalCommunicationParameters: CommunicationParameters) =
        FileInternalCommunicationCleaner.also{
            FileInternalCommunicationCleaner.isAlfdataMounted = internalCommunicationParameters.getIsAlfdataMounted()
        }

}