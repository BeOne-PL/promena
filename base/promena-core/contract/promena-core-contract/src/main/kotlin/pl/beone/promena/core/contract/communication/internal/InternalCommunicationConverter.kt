package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

interface InternalCommunicationConverter {

    fun convert(dataDescriptors: DataDescriptors, transformedDataDescriptors: TransformedDataDescriptors): TransformedDataDescriptors
}