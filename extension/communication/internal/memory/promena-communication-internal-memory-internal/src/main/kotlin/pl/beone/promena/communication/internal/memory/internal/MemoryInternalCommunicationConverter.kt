package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryInternalCommunicationConverter::class.java)
    }

    override fun convert(dataDescriptors: List<DataDescriptor>,
                         transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor> =
            transformedDataDescriptors.map { convertIfNecessary(it) }

    private fun convertIfNecessary(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
        val data = transformedDataDescriptor.data
        return if (data !is MemoryData) {
            logger.warn("One of transformed data in internal communication is type <{}> but should be <MemoryData>. You should use the same data implementation for performance reasons",
                        data::class.java.simpleName)
            TransformedDataDescriptor(data.createMemoryDataAndDeleteOldDataResource(), transformedDataDescriptor.metadata)
        } else {
            transformedDataDescriptor
        }
    }

    private fun Data.createMemoryDataAndDeleteOldDataResource(): MemoryData {
        logger.debug("Creating <MemoryData> from <{}>...", this.toSimplifiedString())
        val memoryData = this.toMemoryData()
        logger.debug("Finished creating <MemoryData> from <{}>", this.toSimplifiedString())

        logger.debug("Deleting <{}> resource...", this.toSimplifiedString())
        try {
            this.delete()
        } catch (e: Exception) {
            when (e) {
                is UnsupportedOperationException,
                is DataDeleteException -> logger.debug("Couldn't delete <{}> resource", this.toSimplifiedString(), e)

                else                   -> throw e
            }
        }
        logger.debug("Finished deleting <{}> resource", this.toSimplifiedString())

        return memoryData
    }

    private fun Data.toSimplifiedString(): String =
            try {
                "${this.javaClass.simpleName}(location=${getLocation()})"
            } catch (e: Exception) {
                "${this.javaClass.simpleName}(location=<isn't available>)"
            }

    private fun Data.toMemoryData(): MemoryData =
            MemoryData(this.getBytes())
}

