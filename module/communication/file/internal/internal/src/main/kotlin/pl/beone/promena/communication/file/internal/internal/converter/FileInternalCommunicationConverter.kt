package pl.beone.promena.communication.file.internal.internal.converter

import pl.beone.promena.communication.file.model.common.converter.FileDescriptorConverter
import pl.beone.promena.communication.file.model.common.converter.MountedFileDescriptorConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import java.io.File

/**
 * Creates a new instance that is the type of [FileData][pl.beone.promena.transformer.internal.model.data.file.FileData]
 * with a file in [directory].
 */
class FileInternalCommunicationConverter(
    directory: File,
    alfdataAlfrescoMountPath: File,
    alfdataPromenaMountPath: File,
    private val isAlfdataMounted: Boolean
) : InternalCommunicationConverter {

    private val fileDescriptorConverter = FileDescriptorConverter(directory)
    private val mountedFileDescriptorConverter = MountedFileDescriptorConverter(alfdataAlfrescoMountPath, alfdataPromenaMountPath)

    override fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        if (isAlfdataMounted) mountedFileDescriptorConverter.convert(dataDescriptor, requireNewInstance)
        else fileDescriptorConverter.convert(dataDescriptor, requireNewInstance)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        if (!isAlfdataMounted) fileDescriptorConverter.convert(transformedDataDescriptor, requireNewInstance)
        else if (!requireNewInstance) transformedDataDescriptor
        else throw UnsupportedOperationException("Creating new instance with mounted Alfdata is not allowed because Alfdata is read only for Promena.")

}