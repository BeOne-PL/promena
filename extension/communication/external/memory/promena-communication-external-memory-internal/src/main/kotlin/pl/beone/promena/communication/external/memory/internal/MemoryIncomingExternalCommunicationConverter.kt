package pl.beone.promena.communication.external.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.communication.internal.memory.internal.convertIfItIsNecessary
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

class MemoryIncomingExternalCommunicationConverter : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryIncomingExternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>, externalCommunicationParameters: CommunicationParameters): List<DataDescriptor> =
            convertIfItIsNecessary(logger, dataDescriptors) { newData, oldDescriptor ->
                DataDescriptor(newData, oldDescriptor.mediaType, oldDescriptor.metadata)
            }

}
