package pl.beone.promena.communication.memory.internal

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryIncomingCommunicationConverter : IncomingCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor, communicationParameters: CommunicationParameters): DataDescriptor =
            dataDescriptor

}

