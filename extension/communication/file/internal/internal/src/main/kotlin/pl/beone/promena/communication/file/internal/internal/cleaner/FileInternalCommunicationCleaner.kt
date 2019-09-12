package pl.beone.promena.communication.file.internal.internal.cleaner

import pl.beone.promena.communication.file.model.common.cleaner.FileDataDescriptorCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class FileInternalCommunicationCleaner : InternalCommunicationCleaner {

    override fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor) {
        FileDataDescriptorCleaner.clean(dataDescriptor, transformedDataDescriptor)
    }
}