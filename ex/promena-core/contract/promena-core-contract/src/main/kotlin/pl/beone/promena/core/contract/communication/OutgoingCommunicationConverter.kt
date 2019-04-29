package pl.beone.promena.core.contract.communication

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface OutgoingCommunicationConverter {

    fun convert(transformedDataDescriptor: TransformedDataDescriptor, communicationParameters: CommunicationParameters): TransformedDataDescriptor
}