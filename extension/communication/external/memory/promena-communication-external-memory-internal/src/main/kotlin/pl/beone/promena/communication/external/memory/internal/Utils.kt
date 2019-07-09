package pl.beone.promena.communication.external.memory.internal

import org.slf4j.Logger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

internal fun Data.toSimplifiedString(): String =
        try {
            "${this::class.java.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this::class.java.simpleName}(location=<isn't available>)"
        }

internal fun Data.createMemoryDataAndDeleteOldDataResource(logger: Logger): MemoryData {
    logger.debug("Creating <MemoryData> from <{}>...", this.toSimplifiedString())
    val convertedData = MemoryData(this.getBytes())
    logger.debug("Finished creating <MemoryData> from <{}>", this.toSimplifiedString())

    logger.debug("Deleting <{}> resource...", this.toSimplifiedString())
    try {
        this.delete()
    } catch (e: Exception) {
        when (e) {
            is UnsupportedOperationException, is DataDeleteException -> logger.debug("Couldn't delete <{}> resource", this.toSimplifiedString(), e)
            else                                                     -> throw e
        }
    }
    logger.debug("Finished deleting <{}> resource", this.toSimplifiedString())

    return convertedData
}