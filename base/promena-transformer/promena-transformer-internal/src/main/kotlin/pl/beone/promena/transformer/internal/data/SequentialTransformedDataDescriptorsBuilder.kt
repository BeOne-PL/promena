package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class SequentialTransformedDataDescriptorsBuilder internal constructor(private val transformedDataDescriptors: MutableList<TransformedDataDescriptor> = ArrayList()) {

    fun and(transformedDataDescriptor: TransformedDataDescriptor): SequentialTransformedDataDescriptorsBuilder =
            apply { transformedDataDescriptors.add(transformedDataDescriptor) }

    fun and(data: Data, metadata: Metadata): SequentialTransformedDataDescriptorsBuilder =
            apply { transformedDataDescriptors.add(TransformedDataDescriptor.of(data, metadata)) }

    fun build(): SequentialTransformedDataDescriptors = SequentialTransformedDataDescriptors(transformedDataDescriptors)

}