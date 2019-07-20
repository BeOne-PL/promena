package pl.beone.promena.core.contract.transformation

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.transformation.Transformation

interface TransformationUseCase {

    fun transform(transformation: Transformation,
                  dataDescriptor: DataDescriptor,
                  externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptors
}