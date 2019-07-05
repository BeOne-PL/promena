package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface OutgoingExternalCommunicationConverter {

    fun convert(transformedDataDescriptors: List<TransformedDataDescriptor>,
                externalCommunicationParameters: CommunicationParameters,
                internalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor>
}