package pl.beone.promena.communication.external.memory.internal

import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

private data class DescriptorWithConvertedData(val convertedData: Data, val mediaType: MediaType, val metadata: Metadata)

class MemoryIncomingExternalCommunicationConverter(private val internalCommunicationConverter: InternalCommunicationConverter) : IncomingExternalCommunicationConverter {

    override fun convert(dataDescriptors: List<DataDescriptor>, externalCommunicationParameters: CommunicationParameters): List<DataDescriptor> =
            zipWithConvertedData(dataDescriptors)
                    .map { it.toDataDescriptor() }

    private fun zipWithConvertedData(dataDescriptors: List<DataDescriptor>): List<DescriptorWithConvertedData> =
            dataDescriptors.zip(internalCommunicationConverter.convert(dataDescriptors, emptyList()))
                    .map { DescriptorWithConvertedData(it.second.data, it.first.mediaType, it.first.metadata) }

    private fun DescriptorWithConvertedData.toDataDescriptor(): DataDescriptor =
            DataDescriptor(convertedData, mediaType, metadata)
}
