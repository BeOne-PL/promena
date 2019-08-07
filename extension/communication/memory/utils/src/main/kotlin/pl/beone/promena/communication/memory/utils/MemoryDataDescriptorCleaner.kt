package pl.beone.promena.communication.memory.utils

import pl.beone.promena.communication.utils.deleter.AbstractDataDescriptorCleaner
import pl.beone.promena.transformer.contract.model.Data

object MemoryDataDescriptorCleaner : AbstractDataDescriptorCleaner() {

    // Memory is managed by JVM
    override fun areTheSame(data: Data, data2: Data): Boolean =
        true
}