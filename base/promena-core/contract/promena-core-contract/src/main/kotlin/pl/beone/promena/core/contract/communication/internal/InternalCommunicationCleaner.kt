package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

interface InternalCommunicationCleaner {

    fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor)
}