package pl.beone.promena.core.contract.communication.internal

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

/**
 * Adjusts data within *internal communication* to be compatible with *internal communication*.
 * It may happen that a transformer returns data of an implementation that isn't compatible with *internal communication*.
 */
interface InternalCommunicationConverter {

    /**
     * Converts [dataDescriptor] into another [DataDescriptor].
     * If [requireNewInstance] is `true`, a new instance is always created.
     *
     * @return a converted data descriptor
     */
    fun convert(dataDescriptor: DataDescriptor, requireNewInstance: Boolean): DataDescriptor

    /**
     * Converts [transformedDataDescriptor] into another [TransformedDataDescriptor].
     * If [requireNewInstance] is `true`, a new instance is always created.
     *
     * @return a converted transformed data descriptor
     */
    fun convert(transformedDataDescriptor: TransformedDataDescriptor, requireNewInstance: Boolean): TransformedDataDescriptor
}