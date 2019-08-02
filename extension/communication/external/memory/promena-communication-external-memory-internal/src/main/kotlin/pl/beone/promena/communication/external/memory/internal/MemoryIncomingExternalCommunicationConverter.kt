package pl.beone.promena.communication.external.memory.internal

import mu.KotlinLogging
import pl.beone.promena.communication.internal.memory.internal.convertIfItIsNecessary
import pl.beone.promena.communication.internal.memory.internal.filterNotMemoryData
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

class MemoryIncomingExternalCommunicationConverter : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        if (isAtLeastOneNotMemoryData(dataDescriptor)) {
            logger.warn { "One of data using in the communication isn't type of <MemoryData>. You should use the same communication implementation (internal and external) for performance reasons" }
        }

        return convertIfItIsNecessary(logger, dataDescriptor)
    }

    private fun isAtLeastOneNotMemoryData(dataDescriptor: DataDescriptor): Boolean =
        dataDescriptor.descriptors
            .filterNotMemoryData { it.data }
            .isNotEmpty()

}
