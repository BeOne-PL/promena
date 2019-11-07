package pl.beone.promena.communication.memory.model.common.cleaner

import pl.beone.promena.communication.common.cleaner.AbstractDataDescriptorCleaner
import pl.beone.promena.transformer.contract.model.data.Data

object MemoryDataDescriptorCleaner : AbstractDataDescriptorCleaner() {

    // Memory is managed by JVM
    override fun areTheSame(data: Data, data2: Data): Boolean =
        true
}