package pl.beone.promena.transformer.contract

import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

/**
 * It is the bridge between a transformer and Promena.
 * Every transformer has to implement this interface.
 */
interface Transformer {

    /**
     * Performs a transformation using the passed parameters.
     *
     * @throws TimeoutException if the timeout from [parameters] (if it is present) or default timeout is exceeded.
     */
    fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor

    /**
     * Checks if it is able to perform a transformation using the passed parameters.
     *
     * @throws TransformationNotSupportedException if the transformer isn't able to transform [dataDescriptor] using [targetMediaType] and [parameters].
     */
    fun isSupported(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters)
}