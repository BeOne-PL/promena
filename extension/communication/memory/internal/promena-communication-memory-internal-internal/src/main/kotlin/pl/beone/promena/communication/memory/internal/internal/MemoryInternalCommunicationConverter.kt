package pl.beone.promena.communication.memory.internal.internal

import pl.beone.promena.communication.memory.utils.MemoryDataDescriptorDeleter
import pl.beone.promena.communication.memory.utils.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
        MemoryDataDescriptorDeleter.delete(dataDescriptor, transformedDataDescriptor)
        return MemoryDescriptorConverter.convert(transformedDataDescriptor)
    }
}