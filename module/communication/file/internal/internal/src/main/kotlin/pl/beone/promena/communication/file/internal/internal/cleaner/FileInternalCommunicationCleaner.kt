package pl.beone.promena.communication.file.internal.internal.cleaner

import pl.beone.promena.communication.file.model.common.cleaner.FileDataDescriptorCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Checks if both data are the type of ([FileData][pl.beone.promena.transformer.internal.model.data.file.FileData])
 * and their [FileData.getLocation][pl.beone.promena.transformer.internal.model.data.file.FileData.getLocation] indicate the same location,
 * If the condition is false, it removes resources associated with given [Data][pl.beone.promena.transformer.contract.model.data.Data] implementation.
 */
object FileInternalCommunicationCleaner : InternalCommunicationCleaner {

    override fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor) {
        FileDataDescriptorCleaner.clean(dataDescriptor, transformedDataDescriptor)
    }
}