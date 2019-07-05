package pl.beone.promena.communication.external.memory.internal

import org.slf4j.Logger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.InMemoryData

internal fun Data.toSimplifiedString(): String =
        try {
            "${this::class.java.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this::class.java.simpleName}(location=<isn't available>)"
        }

internal fun Data.createInMemoryDataAndDeleteOldDataResource(logger: Logger): InMemoryData {
    logger.debug("Creating <InMemoryData> from <{}>...", this.toSimplifiedString())
    val convertedData = InMemoryData(this.getBytes())
    logger.debug("Finished creating <InMemoryData> from <{}>", this.toSimplifiedString())

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