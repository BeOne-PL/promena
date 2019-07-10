package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface OutgoingExternalCommunicationConverter {

    fun convert(transformedDataDescriptors: List<TransformedDataDescriptor>,
                externalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor>
}