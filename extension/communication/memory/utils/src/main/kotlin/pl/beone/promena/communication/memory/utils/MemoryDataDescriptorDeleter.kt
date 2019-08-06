package pl.beone.promena.communication.memory.utils

import pl.beone.promena.communication.utils.deleter.AbstractDataDescriptorDeleter
import pl.beone.promena.transformer.contract.model.Data

object MemoryDataDescriptorDeleter : AbstractDataDescriptorDeleter() {

    // Memory is managed by JVM
    override fun areTheSame(data: Data, data2: Data): Boolean =
        true
}