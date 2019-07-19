package pl.beone.promena.core.contract.transformation

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

interface TransformationUseCase {

    fun transform(transformationFlow: TransformationFlow,
                  dataDescriptors: DataDescriptors,
                  externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptors
}