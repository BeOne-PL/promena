package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

interface InternalCommunicationConverter {

    fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor

    fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor
}