package pl.beone.promena.communication.internal.memory.internal

import mu.KotlinLogging
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.toDataDescriptor
import pl.beone.promena.transformer.internal.model.data.MemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun convert(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
        tryToRemoveResources(dataDescriptor)
        return convertIfItIsNecessary(logger, transformedDataDescriptor)
    }

    private fun tryToRemoveResources(dataDescriptor: DataDescriptor) {
        val notMemoryDataDescriptors = dataDescriptor.filterNotMemoryData().descriptors

        if (notMemoryDataDescriptors.isNotEmpty()) {
            notMemoryDataDescriptors
                .also { logger.debug { "There are <${it.size}> other than <MemoryData> data instances" } }
                .also { logger.debug { "Deleting..." } }
                .map { deleteDataIfItIsPossible(logger, it.data) }
                .also { logger.debug { "Finished deleting" } }
        }
    }

    private fun DataDescriptor.filterNotMemoryData(): DataDescriptor =
        descriptors.filter { it.data !is MemoryData }
            .toDataDescriptor()
}

