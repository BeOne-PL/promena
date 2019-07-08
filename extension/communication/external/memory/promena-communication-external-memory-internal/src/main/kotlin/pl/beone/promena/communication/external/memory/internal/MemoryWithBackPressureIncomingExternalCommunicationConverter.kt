package pl.beone.promena.communication.external.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryWithBackPressureIncomingExternalCommunicationConverter : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryWithBackPressureIncomingExternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>,
                         externalCommunicationParameters: CommunicationParameters,
                         internalCommunicationParameters: CommunicationParameters): List<DataDescriptor> {
        val externalCommunicationId = externalCommunicationParameters.getId()
        val internalCommunicationId = internalCommunicationParameters.getId()
        return if (externalCommunicationId != internalCommunicationId) {
            logger.warn("External communication is <{}> but internal communication is <{}>. You should choose the same communication implementation for performance reasons",
                        externalCommunicationId, internalCommunicationId)

            dataDescriptors.convertToInMemoryDataDescriptors()
        } else {
            dataDescriptors
        }
    }

    private fun List<DataDescriptor>.convertToInMemoryDataDescriptors(): List<DataDescriptor> =
            map { DataDescriptor(it.data.createInMemoryDataAndDeleteOldDataResource(logger), it.mediaType, it.metadata) }
}

