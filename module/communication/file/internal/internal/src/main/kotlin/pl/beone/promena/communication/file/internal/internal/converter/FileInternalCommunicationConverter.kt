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
    sourceFileVolumeExternalMountPath: File,
    sourceFileVolumePromenaMountPath: File,
    private val isSourceFileVolumeMounted: Boolean
) : InternalCommunicationConverter {

    private val fileDescriptorConverter = FileDescriptorConverter(directory)
    private val mountedFileDescriptorConverter = MountedFileDescriptorConverter(sourceFileVolumeExternalMountPath, sourceFileVolumePromenaMountPath)

    override fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor =
        if (isSourceFileVolumeMounted) mountedFileDescriptorConverter.convert(dataDescriptor, requireNewInstance)
        else fileDescriptorConverter.convert(dataDescriptor, requireNewInstance)

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor =
        if (!isSourceFileVolumeMounted) fileDescriptorConverter.convert(transformedDataDescriptor, requireNewInstance)
        else if (!requireNewInstance) transformedDataDescriptor
        else throw UnsupportedOperationException("Creating new instance with mounted SourceFileVolume is not allowed because SourceFileVolume is read only for Promena.")

}