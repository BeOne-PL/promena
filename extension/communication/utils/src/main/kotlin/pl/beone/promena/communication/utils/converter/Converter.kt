package pl.beone.promena.communication.utils.converter

import mu.KotlinLogging
import pl.beone.promena.communication.utils.deleteData
import pl.beone.promena.communication.utils.toSimplifiedString
import pl.beone.promena.transformer.applicationmodel.exception.data.DataOperationException
import pl.beone.promena.transformer.contract.model.Data

internal class Converter<T : Data, D>(
    private val getData: (descriptor: D) -> Data,
    private val createDescriptor: (newData: Data, oldDescriptor: D) -> D,
    private val convertData: (data: Data) -> T,
    private val communicationDescriptor: () -> String,
    private val isCompatible: (data: Data) -> Boolean
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun convert(descriptors: List<D>): List<D> {
        val notCompatibleDescriptors = filterNotCompatibleDescriptors(descriptors)
        val compatibleDescriptors = descriptors - notCompatibleDescriptors

        val convertedNotCompatibleDescriptors =
            if (notCompatibleDescriptors.isNotEmpty()) {
                notCompatibleDescriptors
                    .also { "There are <${descriptors.size}> incompatible descriptors with communication <${communicationDescriptor()}>" }
                    .also { logger.debug { "Converting..." } }
                    .map { descriptor -> processData(getData(descriptor)) to descriptor }
                    .map { (newData, oldDescriptor) -> createDescriptor(newData, oldDescriptor) }
                    .also { logger.debug { "Finished converting" } }
            } else {
                emptyList()
            }

        return compatibleDescriptors + convertedNotCompatibleDescriptors
    }

    private fun processData(data: Data): T =
        createData(data)
            .also { deleteData(logger, data) }

    private fun filterNotCompatibleDescriptors(descriptors: List<D>): List<D> =
        descriptors.filter { !isCompatible(getData(it)) }

    private fun createData(data: Data): T =
        this
            .also { logger.debug { "Creating data from <${data.toSimplifiedString()}>..." } }
            .let { createDataAndHandleExceptions(data) }
            .also { logger.debug { "Finished creating data from <${data.toSimplifiedString()}>" } }

    private fun createDataAndHandleExceptions(data: Data): T =
        try {
            convertData(data)
        } catch (e: Exception) {
            throw "Couldn't create data for communication <${communicationDescriptor()}> from <${data.toSimplifiedString()}>"
                .also { logger.error(e) { it } }
                .let { DataOperationException(it, e) }
        }

}