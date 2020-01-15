package pl.beone.promena.communication.memory.internal.internal.cleaner

import pl.beone.promena.communication.memory.model.common.cleaner.MemoryDataDescriptorCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Memory is managed by Garbage Collector so it just loses the reference to data objects.
 */
object MemoryInternalCommunicationCleaner : InternalCommunicationCleaner {

    override fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor) {
        MemoryDataDescriptorCleaner.clean(dataDescriptor, transformedDataDescriptor)
    }
}