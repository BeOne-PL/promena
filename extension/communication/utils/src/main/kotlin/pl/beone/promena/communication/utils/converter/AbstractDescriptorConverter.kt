package pl.beone.promena.communication.utils.converter

import pl.beone.promena.transformer.contract.data.*
import pl.beone.promena.transformer.contract.model.Data

abstract class AbstractDescriptorConverter<T : Data> {

    private val dataDescriptorConverter = Converter<T, DataDescriptor.Single>(
        true,
        { it.data },
        { newData, oldDescriptor -> singleDataDescriptor(newData, oldDescriptor.mediaType, oldDescriptor.metadata) },
        { convertData(it) },
        { communicationDescriptor() },
        { isCompatible(it) }
    )

    private val transformedDataDescriptorConverter = Converter<T, TransformedDataDescriptor.Single>(
        false,
        { it.data },
        { newData, oldDescriptor -> singleTransformedDataDescriptor(newData, oldDescriptor.metadata) },
        { convertData(it) },
        { communicationDescriptor() },
        { isCompatible(it) }
    )

    protected abstract fun convertData(data: Data): T

    protected abstract fun communicationDescriptor(): String

    protected abstract fun isCompatible(data: Data): Boolean

    fun convert(dataDescriptor: DataDescriptor): DataDescriptor =
        dataDescriptorConverter.convert(dataDescriptor.descriptors).toDataDescriptor()

    fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
        transformedDataDescriptorConverter.convert(transformedDataDescriptor.descriptors).toTransformedDataDescriptor()
}