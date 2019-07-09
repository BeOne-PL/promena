package pl.beone.promena.communication.external.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class MemoryWithBackPressureOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryWithBackPressureOutgoingExternalCommunicationConverter::class.java)
    }

    override fun convert(transformedDataDescriptors: List<TransformedDataDescriptor>,
                         externalCommunicationParameters: CommunicationParameters,
                         internalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor> {
        val externalCommunicationId = externalCommunicationParameters.getId()
        val internalCommunicationId = internalCommunicationParameters.getId()
        return if (externalCommunicationId != internalCommunicationId) {
            logger.warn("External communication is <{}> but internal communication is <{}>. You should choose the same communication implementation for performance reasons",
                        externalCommunicationId, internalCommunicationId)

            transformedDataDescriptors.convertToMemoryTransformedDataDescriptors()
        } else {
            transformedDataDescriptors
        }
    }

    private fun List<TransformedDataDescriptor>.convertToMemoryTransformedDataDescriptors(): List<TransformedDataDescriptor> =
            map { TransformedDataDescriptor(it.data.createMemoryDataAndDeleteOldDataResource(logger), it.metadata) }
}
