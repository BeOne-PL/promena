package pl.beone.promena.core.external.akka.transformation.converter

import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class MirrorInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor): DataDescriptor =
        dataDescriptor

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
        transformedDataDescriptor
}