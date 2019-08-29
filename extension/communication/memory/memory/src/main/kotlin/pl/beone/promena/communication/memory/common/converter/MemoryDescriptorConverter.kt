package pl.beone.promena.communication.memory.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.data.toMemoryData

object MemoryDescriptorConverter : AbstractDescriptorConverter<MemoryData>() {

    override fun communicationDescriptor(): String =
        "memory"

    override fun isCompatible(data: Data): Boolean =
        data is MemoryData

    override fun convertData(data: Data): MemoryData =
        data.getBytes().toMemoryData()
}