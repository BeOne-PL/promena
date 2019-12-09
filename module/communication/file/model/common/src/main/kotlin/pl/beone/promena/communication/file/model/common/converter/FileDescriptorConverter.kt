package pl.beone.promena.communication.file.model.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.communication.file.model.common.extension.isSubPath
import pl.beone.promena.communication.file.model.common.extension.isTheSamePath
import pl.beone.promena.communication.file.model.common.extension.toFile
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData
import pl.beone.promena.transformer.internal.model.data.file.toFileData
import java.io.File

class FileDescriptorConverter(
    private val directory: File
) : AbstractDescriptorConverter<FileData>() {

    override fun communicationDescriptor(): String =
        "${FileCommunicationParametersConstants.ID} (${FileCommunicationParametersConstants.DIRECTORY_KEY}=$directory)"

    override fun isCompatible(data: Data): Boolean =
        data is FileData &&
                (data.getLocation().toFile().isTheSamePath(directory) || data.getLocation().toFile().isSubPath(directory))

    override fun convertData(data: Data): FileData =
        data.getInputStream().toFileData(directory)
}