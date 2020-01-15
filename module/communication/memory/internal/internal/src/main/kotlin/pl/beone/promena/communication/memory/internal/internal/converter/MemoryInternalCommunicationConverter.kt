package pl.beone.promena.communication.memory.internal.internal.converter

import pl.beone.promena.communication.memory.model.common.converter.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Creates a new instance that is the type of [MemoryData][pl.beone.promena.transformer.internal.model.data.memory.MemoryData].
 */
object MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    override fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        MemoryDescriptorConverter.convert(dataDescriptor, requireNewInstance)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        MemoryDescriptorConverter.convert(transformedDataDescriptor, requireNewInstance)
}