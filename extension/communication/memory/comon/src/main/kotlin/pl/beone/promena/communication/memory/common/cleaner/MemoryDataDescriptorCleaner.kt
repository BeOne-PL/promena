package pl.beone.promena.communication.memory.common.cleaner

import pl.beone.promena.communication.common.cleaner.AbstractDataDescriptorCleaner
import pl.beone.promena.transformer.contract.model.Data

object MemoryDataDescriptorCleaner : AbstractDataDescriptorCleaner() {

    // Memory is managed by JVM
    override fun areTheSame(data: Data, data2: Data): Boolean =
        true
}