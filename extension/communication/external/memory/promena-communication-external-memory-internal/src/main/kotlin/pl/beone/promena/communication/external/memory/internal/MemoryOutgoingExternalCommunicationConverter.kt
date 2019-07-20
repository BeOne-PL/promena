package pl.beone.promena.communication.external.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.communication.internal.memory.internal.convertIfItIsNecessary
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

class MemoryOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryOutgoingExternalCommunicationConverter::class.java)
    }

    override fun convert(transformedDataDescriptors: TransformedDataDescriptors,
                         externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptors =
            convertIfItIsNecessary(logger, transformedDataDescriptors)
}

