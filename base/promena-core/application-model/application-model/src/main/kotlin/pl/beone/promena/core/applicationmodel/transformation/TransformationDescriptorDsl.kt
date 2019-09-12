@file:JvmName("TransformationDescriptorDsl")

package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

fun transformationDescriptor(
    transformation: Transformation,
    dataDescriptor: DataDescriptor,
    communicationParameters: CommunicationParameters
): TransformationDescriptor =
    TransformationDescriptor.of(transformation, dataDescriptor, communicationParameters)