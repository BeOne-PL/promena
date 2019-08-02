package pl.beone.promena.communication.external.memory.internal

import mu.KotlinLogging
import pl.beone.promena.communication.internal.memory.internal.convertIfItIsNecessary
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

class MemoryOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(
        transformedDataDescriptor: TransformedDataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor =
        convertIfItIsNecessary(logger, transformedDataDescriptor)
}

