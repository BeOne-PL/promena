package pl.beone.promena.core.external.akka.transformation.converter

import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

class MirrorInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptors: DataDescriptors, transformedDataDescriptors: TransformedDataDescriptors): TransformedDataDescriptors =
            transformedDataDescriptors

}