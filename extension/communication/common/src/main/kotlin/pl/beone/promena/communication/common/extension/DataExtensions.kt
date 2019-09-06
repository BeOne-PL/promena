package pl.beone.promena.communication.common.extension

import mu.KLogger
import pl.beone.promena.transformer.applicationmodel.exception.data.DataOperationException
import pl.beone.promena.transformer.contract.model.Data

internal fun Data.deleteAndLog(logger: KLogger) {
    logger.debug { "Deleting <${toSimplifiedString()}> resource..." }
    this.deleteAndHandleExceptions(logger)
    logger.debug { "Finished deleting <${toSimplifiedString()}> resource" }
}

private fun Data.deleteAndHandleExceptions(logger: KLogger) {
    try {
        delete()
    } catch (e: Exception) {
        if (e !is UnsupportedOperationException) {
            val message = "Couldn't delete <${toSimplifiedString()}> resource"
            logger.error(e) { message }
            throw  DataOperationException(message, e)
        }
    }
}

internal fun Data.toSimplifiedString(): String =
    try {
        "${this.javaClass.simpleName}(location=${getLocation()})"
    } catch (e: Exception) {
        "${this.javaClass.simpleName}(location=<isn't available>)"
    }