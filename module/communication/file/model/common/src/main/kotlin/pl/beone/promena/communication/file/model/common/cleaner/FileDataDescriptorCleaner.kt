package pl.beone.promena.communication.file.model.common.cleaner

import pl.beone.promena.communication.common.cleaner.AbstractDataDescriptorCleaner
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.FileData

object FileDataDescriptorCleaner : AbstractDataDescriptorCleaner() {

    override fun areTheSame(data: Data, data2: Data): Boolean =
        data is FileData && data2 is FileData && data.getLocation() == data2.getLocation()
}