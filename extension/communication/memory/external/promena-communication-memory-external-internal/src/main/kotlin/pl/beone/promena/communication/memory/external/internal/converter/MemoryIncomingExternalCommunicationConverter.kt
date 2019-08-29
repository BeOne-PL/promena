package pl.beone.promena.communication.memory.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

class MemoryIncomingExternalCommunicationConverter(
    private val externalCommunicationId: String,
    private val internalCommunicationId: String,
    private val internalCommunicationConverter: InternalCommunicationConverter
) : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        logger.warnIfCommunicationsAreDifferent(internalCommunicationId, externalCommunicationId)
        return internalCommunicationConverter.convert(dataDescriptor)
    }
}
