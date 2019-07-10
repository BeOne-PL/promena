package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.MemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryInternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>,
                         transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor> {
        tryToRemoveResources(dataDescriptors)
        return convertIfItIsNecessary(logger, transformedDataDescriptors)
    }

    private fun tryToRemoveResources(dataDescriptors: List<DataDescriptor>) {
        val notMemoryDataDescriptors = dataDescriptors.filterNotMemoryData()

        if(notMemoryDataDescriptors.isNotEmpty()) {
            notMemoryDataDescriptors
                    .also { logger.debug("There are <{}> other than <MemoryData> data instances", it.size) }
                    .also { logger.debug("Deleting...") }
                    .map { deleteDataIfItIsPossible(logger, it.data) }
                    .also { logger.debug("Finished deleting") }
        }
    }

    private fun List<DataDescriptor>.filterNotMemoryData(): List<DataDescriptor> =
            filter { it.data !is MemoryData }
}

