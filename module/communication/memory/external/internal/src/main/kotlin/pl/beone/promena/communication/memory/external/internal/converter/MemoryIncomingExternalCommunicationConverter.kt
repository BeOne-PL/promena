package pl.beone.promena.communication.memory.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

class MemoryIncomingExternalCommunicationConverter(
    private val internalCommunicationParameters: CommunicationParameters,
    private val internalCommunicationConverter: InternalCommunicationConverter
) : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor {
        logger.warnIfCommunicationsAreDifferent(
            internalCommunicationParameters.getId(),
            MemoryCommunicationParametersConstants.ID
        )
        return internalCommunicationConverter.convert(dataDescriptor)
    }
}
