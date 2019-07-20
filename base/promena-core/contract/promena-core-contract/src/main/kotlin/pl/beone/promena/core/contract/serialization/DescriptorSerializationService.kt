package pl.beone.promena.core.contract.serialization

import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

interface DescriptorSerializationService {

    fun serialize(transformedDataDescriptor: TransformedDataDescriptor): ByteArray

    fun deserialize(bytes: ByteArray): TransformationDescriptor

}