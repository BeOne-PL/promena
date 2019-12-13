package pl.beone.promena.core.external.akka.transformation.converter

import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

object MirrorInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        dataDescriptor

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        transformedDataDescriptor
}