package pl.beone.promena.communication.common.converter

import mu.KotlinLogging
import pl.beone.promena.communication.common.extension.deleteAndLog
import pl.beone.promena.communication.common.extension.toSimplifiedString
import pl.beone.promena.transformer.applicationmodel.exception.data.DataOperationException
import pl.beone.promena.transformer.contract.model.data.Data

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

    fun convert(descriptors: List<D>, requireNewInstance: Boolean): List<D> =
        if (requireNewInstance) {
            convertToNewInstances(descriptors)
        } else {
            convertToNewInstancesOnlyIfDataIsNotCompatible(descriptors)
        }

    private fun convertToNewInstances(descriptors: List<D>): List<D> {
        logger.debug { "Converting to new instances..." }
        return descriptors
            .map { descriptor -> createData(getData(descriptor)) to descriptor }
            .map { (newData, descriptor) -> createDescriptor(newData, descriptor) }
            .also { logger.debug { "Finished converting to new instances" } }
    }

    private fun convertToNewInstancesOnlyIfDataIsNotCompatible(descriptors: List<D>): List<D> {
        val notCompatibleDescriptors = filterNotCompatibleDescriptors(descriptors)
        val compatibleDescriptors = descriptors - notCompatibleDescriptors

        val convertedNotCompatibleDescriptors =
            if (notCompatibleDescriptors.isNotEmpty()) {
                logger.debug { "There are <${descriptors.size}> incompatible descriptors with communication <${communicationDescriptor()}>" }
                logger.debug { "Converting..." }
                notCompatibleDescriptors
                    .map { descriptor -> processData(getData(descriptor)) to descriptor }
                    .map { (newData, descriptor) -> createDescriptor(newData, descriptor) }
                    .also { logger.debug { "Finished converting" } }
            } else {
                emptyList()
            }

        return compatibleDescriptors + convertedNotCompatibleDescriptors
    }

    private fun processData(data: Data): T =
        createData(data)
            .also { data.deleteAndLog(logger) }

    private fun filterNotCompatibleDescriptors(descriptors: List<D>): List<D> =
        descriptors.filter { !isCompatible(getData(it)) }

    private fun createData(data: Data): T {
        logger.debug { "Creating data from <${data.toSimplifiedString()}>..." }
        return createDataAndHandleExceptions(data)
            .also { logger.debug { "Finished creating data from <${data.toSimplifiedString()}>" } }
    }

    private fun createDataAndHandleExceptions(data: Data): T =
        try {
            convertData(data)
        } catch (e: Exception) {
            throw DataOperationException("Couldn't create data for communication <${communicationDescriptor()}> from <${data.toSimplifiedString()}>", e)
                .also { logger.error(e) { it.message } }
        }
}