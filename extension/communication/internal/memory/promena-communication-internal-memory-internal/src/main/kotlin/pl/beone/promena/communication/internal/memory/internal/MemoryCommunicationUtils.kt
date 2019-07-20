package pl.beone.promena.communication.internal.memory.internal

import org.slf4j.Logger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.data.*
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

fun convertIfItIsNecessary(logger: Logger, dataDescriptors: DataDescriptors): DataDescriptors =
        convertIfItIsNecessary(logger, dataDescriptors.descriptors, { it.data }) { newData, oldDescriptor ->
            dataDescriptor(newData, oldDescriptor.mediaType, oldDescriptor.metadata)
        }.toDataDescriptors()

fun convertIfItIsNecessary(logger: Logger, transformedDataDescriptors: TransformedDataDescriptors): TransformedDataDescriptors =
        convertIfItIsNecessary(logger, transformedDataDescriptors.descriptors, { it.data }) { newData, oldDescriptor ->
            transformedDataDescriptor(newData, oldDescriptor.metadata)
        }.toTransformedDataDescriptors()

private fun <T> convertIfItIsNecessary(logger: Logger,
                               descriptors: List<T>,
                               getData: (descriptor: T) -> Data,
                               factory: (newData: Data, oldDescriptor: T) -> T): List<T> {
    val memoryDescriptors = descriptors.filterMemoryData(getData)

    val notMemoryDescriptors = descriptors.filterNotMemoryData(getData)
    val convertedNotMemoryDescriptors = if (notMemoryDescriptors.isNotEmpty()) {
        notMemoryDescriptors
                .also { logger.debug("There are <{}> other than <MemoryData> data instances", it.size) }
                .also { logger.debug("Converting...") }
                .map { convert(logger, getData(it)) to it }
                .map { (newData, oldDescriptor) -> factory(newData, oldDescriptor) }
                .also { logger.debug("Finished converting") }
    } else {
        emptyList()
    }

    return memoryDescriptors + convertedNotMemoryDescriptors
}

fun <T> List<T>.filterNotMemoryData(getData: (descriptor: T) -> Data): List<T> =
        filter { getData(it) !is MemoryData }

private fun <T> List<T>.filterMemoryData(getData: (descriptor: T) -> Data): List<T> =
        filter { getData(it) is MemoryData }

private fun convert(logger: Logger, data: Data): MemoryData {
    val newMemoryData = createMemoryData(logger, data)
    deleteDataIfItIsPossible(logger, data)
    return newMemoryData
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
        MemoryData.of(this.getBytes())