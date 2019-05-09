package pl.beone.promena.communication.memory.internal

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class MemoryOutgoingCommunicationConverter : OutgoingCommunicationConverter {

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor,
                         communicationParameters: CommunicationParameters): TransformedDataDescriptor =
            transformedDataDescriptor

}
