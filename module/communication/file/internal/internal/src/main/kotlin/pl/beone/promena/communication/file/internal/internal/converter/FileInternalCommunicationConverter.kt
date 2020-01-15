package pl.beone.promena.communication.file.internal.internal.converter

import pl.beone.promena.communication.file.model.common.converter.FileDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import java.io.File

/**
 * Creates a new instance that is the type of [FileData][pl.beone.promena.transformer.internal.model.data.file.FileData]
 * with a file in [directory].
 */
class FileInternalCommunicationConverter(
    directory: File
) : InternalCommunicationConverter {

    private val fileDescriptorConverter = FileDescriptorConverter(directory)

    override fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        fileDescriptorConverter.convert(dataDescriptor, requireNewInstance)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        fileDescriptorConverter.convert(transformedDataDescriptor, requireNewInstance)
}