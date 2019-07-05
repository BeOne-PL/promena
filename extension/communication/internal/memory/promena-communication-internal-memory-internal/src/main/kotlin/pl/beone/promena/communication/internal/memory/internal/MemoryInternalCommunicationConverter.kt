package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryInternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>,
                         transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor> =
            transformedDataDescriptors.map { convertIfNecessary(it) }

    private fun convertIfNecessary(it: TransformedDataDescriptor): TransformedDataDescriptor {
        val data = it.data
        return if (data !is InMemoryData) {
            logger.warn("One of transformed data in internal communication is type <{}> but should be <InMemoryData>. You should use the same data implementation for performance reasons",
                        data::class.java.simpleName)
            TransformedDataDescriptor(data.createInMemoryDataAndDeleteOldDataResource(logger), it.metadata)
        } else {
            it
        }
    }

}

