package pl.beone.promena.communication.file.model.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData
import java.io.File

class MountedFileDescriptorConverter(
    private val alfdataAlfrescoMountDir: File,
    private val alfdataPromenaMountDir: File
): AbstractDescriptorConverter<FileData>() {

    override fun convertData(data: Data): FileData =
        FileData.of(
            File(data.getLocation()
                    .toASCIIString()
                    .removePrefix("file:")
                    .replace(alfdataAlfrescoMountDir.path, alfdataPromenaMountDir.path)
            )
        )

    override fun communicationDescriptor(): String =
        "${FileCommunicationParametersConstants.ID} - ${FileCommunicationParametersConstants.IS_ALFDATA_MOUNTED_KEY}" +
                "${FileCommunicationParametersConstants.ALFDATA_MOUNT_PATH_KEY}: $alfdataPromenaMountDir"

    override fun isCompatible(data: Data): Boolean =
        data is FileData

}