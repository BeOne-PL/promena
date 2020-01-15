package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Cleans resources associated with data after a transformation execution.
 */
interface InternalCommunicationCleaner {

    /**
     * Cleans the resources associated with [dataDescriptor] if there are no references in [transformedDataDescriptor].
     */
    fun clean(dataDescriptor: DataDescriptor, transformedDataDescriptor: TransformedDataDescriptor)
}