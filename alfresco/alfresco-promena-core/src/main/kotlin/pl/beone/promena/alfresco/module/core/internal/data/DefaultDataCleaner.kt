package pl.beone.promena.alfresco.module.core.internal.data

import mu.KotlinLogging
import pl.beone.promena.alfresco.module.core.contract.data.DataCleaner
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.data.Data

class DefaultDataCleaner : DataCleaner {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun clean(datas: List<Data>) {
        datas.forEach(::deleteData)
    }

    private fun deleteData(data: Data) {
        logger.debug { "Deleting <${data.toSimplifiedString()}> resource..." }
        try {
            data.delete()
        } catch (e: Exception) {
            when (e) {
                is UnsupportedOperationException,
                is DataDeleteException -> logger.debug(e) { "Couldn't delete <${data.toSimplifiedString()}> resource" }
                else -> throw e
            }
        }
        logger.debug { "Finished deleting <${data.toSimplifiedString()}> resource" }
    }

    private fun Data.toSimplifiedString(): String =
        try {
            "${this::class.java.simpleName}(location=${getLocation()})"
        } catch (e: Exception) {
            "${this::class.java.simpleName}(location=<isn't available>)"
        }
}