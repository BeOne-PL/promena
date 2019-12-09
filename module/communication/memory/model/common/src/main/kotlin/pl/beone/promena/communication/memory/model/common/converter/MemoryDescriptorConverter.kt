package pl.beone.promena.communication.memory.model.common.converter

import pl.beone.promena.communication.common.converter.AbstractDescriptorConverter
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.memory.MemoryData
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData

object MemoryDescriptorConverter : AbstractDescriptorConverter<MemoryData>() {

    override fun communicationDescriptor(): String =
        MemoryCommunicationParametersConstants.ID

    override fun isCompatible(data: Data): Boolean =
        data is MemoryData

    override fun convertData(data: Data): MemoryData =
        data.getBytes().toMemoryData()
}