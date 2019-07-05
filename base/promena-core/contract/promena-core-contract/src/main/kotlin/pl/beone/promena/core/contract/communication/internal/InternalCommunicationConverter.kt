package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface InternalCommunicationConverter {

    fun convert(dataDescriptors: List<DataDescriptor>, transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor>
}