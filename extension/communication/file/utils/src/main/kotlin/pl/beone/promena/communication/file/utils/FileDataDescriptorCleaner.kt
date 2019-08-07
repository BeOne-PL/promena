package pl.beone.promena.communication.file.utils

import pl.beone.promena.communication.utils.cleaner.AbstractDataDescriptorCleaner
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData

object FileDataDescriptorCleaner : AbstractDataDescriptorCleaner() {

    override fun areTheSame(data: Data, data2: Data): Boolean =
        data is FileData && data2 is FileData && data.getLocation() == data2.getLocation()
}