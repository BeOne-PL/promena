package pl.beone.promena.communication.common.converter

import pl.beone.promena.transformer.contract.data.*
import pl.beone.promena.transformer.contract.model.data.Data

abstract class AbstractDescriptorConverter<T : Data> {

    private val dataDescriptorConverter = Converter<T, DataDescriptor.Single>(
        { it.data },
        { newData, oldDescriptor -> singleDataDescriptor(newData, oldDescriptor.mediaType, oldDescriptor.metadata) },
        { convertData(it) },
        { communicationDescriptor() },
        { isCompatible(it) }
    )

    private val transformedDataDescriptorConverter = Converter<T, TransformedDataDescriptor.Single>(
        { it.data },
        { newData, oldDescriptor -> singleTransformedDataDescriptor(newData, oldDescriptor.metadata) },
        { convertData(it) },
        { communicationDescriptor() },
        { isCompatible(it) }
    )

    protected abstract fun convertData(data: Data): T

    protected abstract fun communicationDescriptor(): String

    protected abstract fun isCompatible(data: Data): Boolean


    fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        dataDescriptorConverter.convert(dataDescriptor.descriptors, requireNewInstance).toDataDescriptor()

    fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        transformedDataDescriptorConverter.convert(transformedDataDescriptor.descriptors, requireNewInstance).toTransformedDataDescriptor()
}