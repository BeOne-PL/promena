package pl.beone.promena.communication.internal.memory.internal

import mu.KLogger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.data.*
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

fun convertIfItIsNecessary(logger: KLogger, dataDescriptor: DataDescriptor): DataDescriptor =
    convertIfItIsNecessary(logger, dataDescriptor.descriptors, { it.data }) { newData, oldDescriptor ->
        singleDataDescriptor(newData, oldDescriptor.mediaType, oldDescriptor.metadata)
    }.toDataDescriptor()

fun convertIfItIsNecessary(logger: KLogger, transformedDataDescriptor: TransformedDataDescriptor): TransformedDataDescriptor =
    convertIfItIsNecessary(logger, transformedDataDescriptor.descriptors, { it.data }) { newData, oldDescriptor ->
        singleTransformedDataDescriptor(newData, oldDescriptor.metadata)
    }.toTransformedDataDescriptor()

private fun <T> convertIfItIsNecessary(
    logger: KLogger,
    descriptors: List<T>,
    getData: (descriptor: T) -> Data,
    factory: (newData: Data, oldDescriptor: T) -> T
): List<T> {
    val memoryDescriptors = descriptors.filterMemoryData(getData)
    val notMemoryDescriptors = descriptors.filterNotMemoryData(getData)

    val convertedNotMemoryDescriptors = if (notMemoryDescriptors.isNotEmpty()) {
        notMemoryDescriptors
            .also { logger.debug { "There are <${it.size}> other than <MemoryData> data instances" } }
            .also { logger.debug { "Converting..." } }
            .map { convert(logger, getData(it)) to it }
            .map { (newData, oldDescriptor) -> factory(newData, oldDescriptor) }
            .also { logger.debug { "Finished converting" } }
    } else {
        emptyList()
    }

    return memoryDescriptors + convertedNotMemoryDescriptors
}

fun <T> List<T>.filterNotMemoryData(getData: (descriptor: T) -> Data): List<T> =
    filter { getData(it) !is MemoryData }

private fun <T> List<T>.filterMemoryData(getData: (descriptor: T) -> Data): List<T> =
    filter { getData(it) is MemoryData }

private fun convert(logger: KLogger, data: Data): MemoryData =
    createMemoryData(logger, data)
        .also { deleteDataIfItIsPossible(logger, data) }

private fun createMemoryData(logger: KLogger, data: Data): MemoryData {
    val simplifiedString = data.toSimplifiedString()

    logger.debug { "Creating <MemoryData> from <$simplifiedString>..." }
    val memoryData = data.toMemoryData()
    logger.debug { "Finished creating <MemoryData> from <$simplifiedString>" }

    return memoryData
}

internal fun deleteDataIfItIsPossible(logger: KLogger, data: Data) {
    val simplifiedString = data.toSimplifiedString()

    logger.debug { "Deleting <$simplifiedString> resource..." }
    try {
        data.delete()
    } catch (e: Exception) {
        when (e) {
            is UnsupportedOperationException,
            is DataDeleteException -> logger.debug(e) { "Couldn't delete <$simplifiedString> resource" }

            else                   -> throw e
        }
    }
    logger.debug { "Finished deleting <$simplifiedString> resource" }
}

private fun Data.toSimplifiedString(): String =
    try {
        "${this.javaClass.simpleName}(location=${getLocation()})"
    } catch (e: Exception) {
        "${this.javaClass.simpleName}(location=<isn't available>)"
    }

private fun Data.toMemoryData(): MemoryData =
    MemoryData.of(this.getBytes())