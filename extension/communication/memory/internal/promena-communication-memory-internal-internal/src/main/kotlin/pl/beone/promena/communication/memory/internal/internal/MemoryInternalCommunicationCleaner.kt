package pl.beone.promena.communication.memory.internal.internal

import pl.beone.promena.communication.memory.utils.MemoryDataDescriptorCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class MemoryInternalCommunicationCleaner : InternalCommunicationCleaner {

    override fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor) {
        MemoryDataDescriptorCleaner.clean(dataDescriptor, transformedDataDescriptor)
    }
}