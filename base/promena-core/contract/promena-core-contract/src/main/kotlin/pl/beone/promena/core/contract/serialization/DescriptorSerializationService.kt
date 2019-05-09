package pl.beone.promena.core.contract.serialization

import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

interface DescriptorSerializationService {

    fun serialize(transformedDataDescriptors: List<TransformedDataDescriptor>): ByteArray

    fun deserialize(bytes: ByteArray): TransformationDescriptor

}