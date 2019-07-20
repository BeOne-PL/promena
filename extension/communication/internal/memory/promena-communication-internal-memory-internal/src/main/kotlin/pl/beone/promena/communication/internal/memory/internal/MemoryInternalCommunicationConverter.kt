package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.data.toDataDescriptor
import pl.beone.promena.transformer.internal.model.data.MemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryInternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptor: DataDescriptor,
                         transformedDataDescriptors: TransformedDataDescriptors): TransformedDataDescriptors {
        tryToRemoveResources(dataDescriptor)
        return convertIfItIsNecessary(logger, transformedDataDescriptors)
    }

    private fun tryToRemoveResources(dataDescriptor: DataDescriptor) {
        val notMemoryDataDescriptors = dataDescriptor.filterNotMemoryData().descriptors

        if (notMemoryDataDescriptors.isNotEmpty()) {
            notMemoryDataDescriptors
                    .also { logger.debug("There are <{}> other than <MemoryData> data instances", it.size) }
                    .also { logger.debug("Deleting...") }
                    .map { deleteDataIfItIsPossible(logger, it.data) }
                    .also { logger.debug("Finished deleting") }
        }
    }

    private fun DataDescriptor.filterNotMemoryData(): DataDescriptor =
            descriptors.filter { it.data !is MemoryData }
                    .toDataDescriptor()
}

