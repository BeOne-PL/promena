package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

data class TransformationDescriptor internal constructor(
    val transformation: Transformation,
    val dataDescriptor: DataDescriptor,
    val communicationParameters: CommunicationParameters
) {

    companion object {
        @JvmStatic
        fun of(
            transformation: Transformation,
            dataDescriptor: DataDescriptor,
            communicationParameters: CommunicationParameters
        ): TransformationDescriptor =
            TransformationDescriptor(transformation, dataDescriptor, communicationParameters)
    }
}