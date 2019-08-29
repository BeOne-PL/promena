package pl.beone.promena.communication.memory.external.internal.converter

import pl.beone.promena.communication.memory.common.converter.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class MemoryOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        MemoryDescriptorConverter.convert(transformedDataDescriptor)
}

