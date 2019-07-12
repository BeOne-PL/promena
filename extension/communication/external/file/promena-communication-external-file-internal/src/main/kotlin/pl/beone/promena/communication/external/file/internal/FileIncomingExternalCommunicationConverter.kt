package pl.beone.promena.communication.external.file.internal

import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class FileIncomingExternalCommunicationConverter : IncomingExternalCommunicationConverter {

    // deliberately omitted - if communication are different, it will be handled in internal communication
    override fun convert(dataDescriptors: List<DataDescriptor>, externalCommunicationParameters: CommunicationParameters): List<DataDescriptor> =
            dataDescriptors
}
