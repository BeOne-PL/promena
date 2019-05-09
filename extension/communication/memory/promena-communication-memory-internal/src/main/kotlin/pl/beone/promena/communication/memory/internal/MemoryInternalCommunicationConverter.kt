package pl.beone.promena.communication.memory.internal

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData

class MemoryInternalCommunicationConverter : InternalCommunicationConverter {

    companion object {
        private val logger = LoggerFactory.getLogger(MemoryInternalCommunicationConverter::class.java)
    }

    override fun convert(transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
        return try {
            val location = transformedDataDescriptor.data.getLocation()

            logger.warn("Data is located under <$location> location. Loading into memory...")

            TransformedDataDescriptor(InMemoryData(transformedDataDescriptor.data.getBytes()), transformedDataDescriptor.metadata)
        } catch (e: UnsupportedOperationException) {
            // it's deliberately. Memory implementation hasn't location

            transformedDataDescriptor
        }
    }

}

