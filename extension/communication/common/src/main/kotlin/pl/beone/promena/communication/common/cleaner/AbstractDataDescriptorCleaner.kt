package pl.beone.promena.communication.common.cleaner

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.deleteAndLog
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Data

abstract class AbstractDataDescriptorCleaner {

    companion object {
        protected val logger = KotlinLogging.logger {}
    }

    protected abstract fun areTheSame(data: Data, data2: Data): Boolean

    fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor) {
        val transformedDatas = transformedDataDescriptor.getDatas()
        val datasToDelete = dataDescriptor.getDatas()
            .filter { data -> checkIfIsUseInDatas(data, transformedDatas) }

        if (datasToDelete.isNotEmpty()) {
            datasToDelete
                .also { logger.debug { "There are <${it.size}> data from descriptors that aren't used in transformed descriptors" } }
                .also { logger.debug { "Deleting..." } }
                .map { it.deleteAndLog(logger) }
                .also { logger.debug { "Finished deleting" } }
        }
    }

    private fun TransformedDataDescriptor.getDatas(): List<Data> =
        descriptors.map(TransformedDataDescriptor.Single::data)

    private fun DataDescriptor.getDatas(): List<Data> =
        descriptors.map(DataDescriptor.Single::data)

    private fun checkIfIsUseInDatas(data: Data, datas: List<Data>): Boolean =
        datas.none { areTheSame(data, it) }
}