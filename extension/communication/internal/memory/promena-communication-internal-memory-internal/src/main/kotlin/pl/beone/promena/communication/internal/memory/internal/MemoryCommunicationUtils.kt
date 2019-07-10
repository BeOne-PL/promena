package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.Logger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

fun convertIfItIsNecessary(logger: Logger, transformedDataDescriptors: List<TransformedDataDescriptor>): List<TransformedDataDescriptor> {
    val transformedMemoryDataDescriptors = transformedDataDescriptors.filterMemoryData()

    val transformedNotMemoryDataDescriptors = transformedDataDescriptors.filterNotMemoryData()
    val convertedTransformedNotMemoryDataDescriptors = if (transformedNotMemoryDataDescriptors.isNotEmpty()) {
        transformedNotMemoryDataDescriptors
                .also { logger.debug("There are <{}> other than <MemoryData> transformed data instances", it.size) }
                .also { logger.debug("Converting...") }
                .map { convert(logger, it) }
                .also { logger.debug("Finished converting") }
    } else {
        emptyList()
    }

    return transformedMemoryDataDescriptors + convertedTransformedNotMemoryDataDescriptors
}

private fun List<TransformedDataDescriptor>.filterMemoryData(): List<TransformedDataDescriptor> =
        filter { it.data is MemoryData }

private fun List<TransformedDataDescriptor>.filterNotMemoryData(): List<TransformedDataDescriptor> =
        filter { it.data !is MemoryData }

private fun convert(logger: Logger, transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor {
    val data = transformedDataDescriptor.data

    val newMemoryData = createMemoryData(logger, data)
    deleteDataIfItIsPossible(logger, data)
    return TransformedDataDescriptor(newMemoryData, transformedDataDescriptor   .metadata)
}

private fun createMemoryData(logger: Logger, data: Data): MemoryData {
    val simplifiedString = data.toSimplifiedString()

    logger.debug("Creating <MemoryData> from <{}>...", simplifiedString)
    val memoryData = data.toMemoryData()
    logger.debug("Finished creating <MemoryData> from <{}>", simplifiedString)

    return memoryData
}

internal fun deleteDataIfItIsPossible(logger: Logger, data: Data) {
    val simplifiedString = data.toSimplifiedString()

    logger.debug("Deleting <{}> resource...", simplifiedString)
    try {
        data.delete()
    } catch (e: Exception) {
        when (e) {
            is UnsupportedOperationException,
            is DataDeleteException -> logger.debug("Couldn't delete <{}> resource", simplifiedString, e)

            else                   -> throw e
        }
    }
    logger.debug("Finished deleting <{}> resource", simplifiedString)
}

private fun Data.toSimplifiedString(): String =
        try {
            "${this.javaClass.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this.javaClass.simpleName}(location=<isn't available>)"
        }

private fun Data.toMemoryData(): MemoryData =
        MemoryData(this.getBytes())