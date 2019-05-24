package pl.beone.promena.core.contract.transformation

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface TransformationUseCase {

    fun transform(transformerId: String, bytes: ByteArray, communicationParameters: CommunicationParameters): ByteArray

    fun transform(transformerId: String,
                  transformationDescriptor: TransformationDescriptor,
                  communicationParameters: CommunicationParameters): List<TransformedDataDescriptor>
}