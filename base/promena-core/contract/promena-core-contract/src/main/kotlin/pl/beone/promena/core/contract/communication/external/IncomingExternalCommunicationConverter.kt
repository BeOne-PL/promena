package pl.beone.promena.core.contract.communication.external

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

interface IncomingExternalCommunicationConverter {

    fun convert(dataDescriptors: List<DataDescriptor>,
                externalCommunicationParameters: CommunicationParameters,
                internalCommunicationParameters: CommunicationParameters): List<DataDescriptor>
}