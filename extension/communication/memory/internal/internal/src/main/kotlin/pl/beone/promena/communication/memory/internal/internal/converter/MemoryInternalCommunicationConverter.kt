package pl.beone.promena.communication.memory.internal.internal.converter

import pl.beone.promena.communication.memory.model.common.converter.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

object MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor): DataDescriptor =
        MemoryDescriptorConverter.convert(dataDescriptor)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
        MemoryDescriptorConverter.convert(transformedDataDescriptor)
}