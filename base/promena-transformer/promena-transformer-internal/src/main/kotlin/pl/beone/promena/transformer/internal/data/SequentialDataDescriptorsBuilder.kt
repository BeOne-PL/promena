package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class SequentialDataDescriptorsBuilder internal constructor(private val dataDescriptors: MutableList<DataDescriptor> = ArrayList()) {

    fun and(dataDescriptor: DataDescriptor): SequentialDataDescriptorsBuilder =
            apply { dataDescriptors.add(dataDescriptor) }

    fun and(data: Data, mediaType: MediaType, metadata: Metadata): SequentialDataDescriptorsBuilder =
            apply { dataDescriptors.add(DataDescriptor.of(data, mediaType, metadata)) }

    fun build(): SequentialDataDescriptors = SequentialDataDescriptors(dataDescriptors)

}