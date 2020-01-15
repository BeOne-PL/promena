package pl.beone.promena.communication.memory.external.internal.converter

import pl.beone.promena.communication.memory.model.common.converter.MemoryDescriptorConverter
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Converts a data into [MemoryData][pl.beone.promena.transformer.internal.model.data.memory.MemoryData]
 * if [CommunicationParameters.getId] isn't [MemoryCommunicationParametersConstants.ID].
 * If a data is the type of [MemoryData][pl.beone.promena.transformer.internal.model.data.memory.MemoryData], it returns the same instance.
 */
object MemoryOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        MemoryDescriptorConverter.convert(transformedDataDescriptor, false)
}

