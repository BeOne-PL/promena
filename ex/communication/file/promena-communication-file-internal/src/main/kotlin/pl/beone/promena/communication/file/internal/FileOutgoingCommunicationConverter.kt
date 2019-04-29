package pl.beone.promena.communication.file.internal

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data

class FileOutgoingCommunicationConverter :
        OutgoingCommunicationConverter, AbstractFileCommunicationConverter<TransformedDataDescriptor>() {

    companion object {
        private val logger = LoggerFactory.getLogger(FileOutgoingCommunicationConverter::class.java)
    }

    override fun logger(): Logger =
            logger

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor,
                         communicationParameters: CommunicationParameters): TransformedDataDescriptor =
            convertImpl(transformedDataDescriptor, communicationParameters.getLocation(), false)

    override fun convertDescriptor(currentDescriptor: TransformedDataDescriptor, data: Data): TransformedDataDescriptor =
            TransformedDataDescriptor(data, currentDescriptor.metadata)

}
