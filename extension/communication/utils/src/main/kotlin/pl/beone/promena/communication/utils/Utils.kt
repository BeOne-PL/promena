package pl.beone.promena.communication.utils

import mu.KLogger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataOperationException
import pl.beone.promena.transformer.contract.model.Data

internal fun deleteData(logger: KLogger, data: Data) {
    logger.debug { "Deleting <${data.toSimplifiedString()}> resource..." }
    deleteDataAndHandleExceptions(logger, data)
    logger.debug { "Finished deleting <${data.toSimplifiedString()}> resource" }
}

private fun deleteDataAndHandleExceptions(logger: KLogger, data: Data) {
    try {
        data.delete()
    } catch (e: Exception) {
        if (e !is UnsupportedOperationException) {
            throw  "Couldn't delete <${data.toSimplifiedString()}> resource"
                .also { logger.error(e) { it } }
                .let { DataOperationException(it, e) }
        }
    }
}

internal fun Data.toSimplifiedString(): String =
    try {
        "${this.javaClass.simpleName}(location=${getLocation()})"
    } catch (e: Exception) {
        "${this.javaClass.simpleName}(location=<isn't available>)"
    }