package pl.beone.promena.communication.file.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import java.net.URI

class FileIncomingCommunicationConverter(private val location: URI)
    : IncomingCommunicationConverter, AbstractFileCommunicationConverter<DataDescriptor>() {

    companion object {
        private val logger = LoggerFactory.getLogger(FileIncomingCommunicationConverter::class.java)
    }

    override fun logger(): Logger = logger

    override fun convert(dataDescriptor: DataDescriptor, communicationParameters: CommunicationParameters): DataDescriptor =
            convertImpl(dataDescriptor, location, false)

    override fun convertDescriptor(currentDescriptor: DataDescriptor, data: Data): DataDescriptor =
            DataDescriptor(data, currentDescriptor.mediaType)

}

