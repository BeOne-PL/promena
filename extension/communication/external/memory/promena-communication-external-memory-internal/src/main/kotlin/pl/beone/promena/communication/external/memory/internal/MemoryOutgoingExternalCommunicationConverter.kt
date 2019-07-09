package pl.beone.promena.communication.external.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

class MemoryOutgoingExternalCommunicationConverter : OutgoingExternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryOutgoingExternalCommunicationConverter::class.java)
    }

    override fun convert(transformedDataDescriptors: List<TransformedDataDescriptor>,
                         externalCommunicationParameters: CommunicationParameters,
                         internalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor> {
        val externalCommunicationId = externalCommunicationParameters.getId()
        val internalCommunicationId = internalCommunicationParameters.getId()
        return if (externalCommunicationId != internalCommunicationId) {
            logger.warn("External communication is <{}> but internal communication is <{}>. You should choose the same communication implementation for performance reasons",
                        externalCommunicationId, internalCommunicationId)

            transformedDataDescriptors.map { TransformedDataDescriptor(it.data.createMemoryDataAndDeleteOldDataResource(), it.metadata) }
        } else {
            transformedDataDescriptors
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
                is UnsupportedOperationException, is DataDeleteException -> logger.debug("Couldn't delete <{}> resource",
                                                                                         this.toSimplifiedString(),
                                                                                         e)
                else                                                     -> throw e
            }
        }
        logger.debug("Finished deleting <{}> resource", this.toSimplifiedString())

        return memoryData
    }

    private fun Data.toSimplifiedString(): String =
            try {
                "${this::class.java.simpleName}(location=${getLocation()})"
            } catch (e: Exception) {
                "${this::class.java.simpleName}(location=<isn't available>)"
            }

    private fun Data.toMemoryData(): MemoryData =
            MemoryData(this.getBytes())
}

