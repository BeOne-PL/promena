package pl.beone.promena.communication.file.model.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData
import java.io.File

class MountedFileDescriptorConverter(
    private val sourceFileVolumeExternalMountDir: File,
    private val sourceFileVolumePromenaMountDir: File
): AbstractDescriptorConverter<FileData>() {

    override fun convertData(data: Data): FileData =
        FileData.of(
            File(data.getLocation()
                    .path
                    .replace(sourceFileVolumeExternalMountDir.path, sourceFileVolumePromenaMountDir.path)
            )
        )

    override fun communicationDescriptor(): String =
        "${FileCommunicationParametersConstants.ID} - ${FileCommunicationParametersConstants.IS_SOURCE_FILE_VOLUME_MOUNTED_KEY}" +
                "${FileCommunicationParametersConstants.SOURCE_FILE_VOLUME_MOUNT_PATH_KEY}: $sourceFileVolumePromenaMountDir"

    override fun isCompatible(data: Data): Boolean =
        data is FileData

}