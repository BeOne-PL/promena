package pl.beone.promena.communication.memory.internal.internal

import pl.beone.promena.communication.memory.utils.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor): DataDescriptor =
        MemoryDescriptorConverter.convert(dataDescriptor)
}