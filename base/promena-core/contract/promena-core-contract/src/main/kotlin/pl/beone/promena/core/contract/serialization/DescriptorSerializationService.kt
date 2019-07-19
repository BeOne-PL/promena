package pl.beone.promena.core.contract.serialization

import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

interface DescriptorSerializationService {

    fun serialize(transformedDataDescriptors: TransformedDataDescriptors): ByteArray

    fun deserialize(bytes: ByteArray): TransformationDescriptor

}