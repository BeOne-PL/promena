package pl.beone.promena.core.contract.communication

import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

interface IncomingCommunicationConverter {

    fun convert(dataDescriptor: DataDescriptor, communicationParameters: CommunicationParameters): DataDescriptor
}