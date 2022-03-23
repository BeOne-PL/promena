package pl.beone.promena.communication.file.internal.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.converter.FileInternalCommunicationConverter
import pl.beone.promena.communication.file.model.internal.getSourceFileVolumeExternalMountDirectory
import pl.beone.promena.communication.file.model.internal.getSourceFileVolumeMountDirectory
import pl.beone.promena.communication.file.model.internal.getDirectory
import pl.beone.promena.communication.file.model.internal.getIsSourceFileVolumeMounted
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(
        internalCommunicationParameters: CommunicationParameters
    ) =
        FileInternalCommunicationConverter(
            internalCommunicationParameters.getDirectory(),
            internalCommunicationParameters.getSourceFileVolumeExternalMountDirectory(),
            internalCommunicationParameters.getSourceFileVolumeMountDirectory(),
            internalCommunicationParameters.getIsSourceFileVolumeMounted()
        )
}