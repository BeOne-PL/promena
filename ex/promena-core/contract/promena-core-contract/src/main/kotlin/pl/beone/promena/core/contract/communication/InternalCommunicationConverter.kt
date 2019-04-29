package pl.beone.promena.core.contract.communication

import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface InternalCommunicationConverter {

    fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor
}