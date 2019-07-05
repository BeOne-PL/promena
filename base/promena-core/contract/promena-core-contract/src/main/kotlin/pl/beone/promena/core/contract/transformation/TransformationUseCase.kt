package pl.beone.promena.core.contract.transformation

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface TransformationUseCase {

    fun transform(transformerId: String,
                  transformationDescriptor: TransformationDescriptor,
                  externalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor>
}