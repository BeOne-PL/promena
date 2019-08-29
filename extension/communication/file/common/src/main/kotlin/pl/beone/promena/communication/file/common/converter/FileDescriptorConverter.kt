package pl.beone.promena.communication.file.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.communication.file.common.extension.isSubPath
import pl.beone.promena.communication.file.common.extension.isTheSame
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.toFileData
import java.net.URI

class FileDescriptorConverter(private val location: URI) : AbstractDescriptorConverter<FileData>() {

    override fun communicationDescriptor(): String =
        "file (location=$location)"

    override fun isCompatible(data: Data): Boolean =
        data is FileData &&
                (data.getLocation().isTheSame(location) || data.getLocation().isSubPath(location))

    override fun convertData(data: Data): FileData =
        data.getInputStream().toFileData(location)
}