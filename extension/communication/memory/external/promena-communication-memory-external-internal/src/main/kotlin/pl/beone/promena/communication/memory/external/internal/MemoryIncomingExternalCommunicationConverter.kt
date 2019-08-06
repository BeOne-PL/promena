package pl.beone.promena.communication.memory.external.internal

import mu.KotlinLogging
import pl.beone.promena.communication.memory.utils.MemoryDescriptorConverter
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor

class MemoryIncomingExternalCommunicationConverter : IncomingExternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, externalCommunicationParameters: CommunicationParameters): DataDescriptor =
        MemoryDescriptorConverter.convert(dataDescriptor)
}
