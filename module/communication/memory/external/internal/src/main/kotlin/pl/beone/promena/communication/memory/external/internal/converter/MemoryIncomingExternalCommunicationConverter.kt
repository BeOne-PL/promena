package pl.beone.promena.communication.memory.external.internal.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.warnIfCommunicationsAreDifferent
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

/**
 * Converts a data into [MemoryData][pl.beone.promena.transformer.internal.model.data.memory.MemoryData]
 * if [CommunicationParameters.getId] isn't [MemoryCommunicationParametersConstants.ID].
 * If a data is the type of [MemoryData][pl.beone.promena.transformer.internal.model.data.memory.MemoryData], it returns the same instance.
 */
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
        return internalCommunicationConverter.convert(dataDescriptor, false)
    }
}
