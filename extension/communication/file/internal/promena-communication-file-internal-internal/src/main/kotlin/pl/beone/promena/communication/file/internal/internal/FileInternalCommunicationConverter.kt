package pl.beone.promena.communication.file.internal.internal

import pl.beone.promena.communication.file.utils.FileDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import java.net.URI

class FileInternalCommunicationConverter(
    internalCommunicationLocation: URI
) : InternalCommunicationConverter {

    private val fileDescriptorConverter = FileDescriptorConverter(internalCommunicationLocation)

    override fun convert(dataDescriptor: DataDescriptor): DataDescriptor =
        fileDescriptorConverter.convert(dataDescriptor)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
        fileDescriptorConverter.convert(transformedDataDescriptor)
}