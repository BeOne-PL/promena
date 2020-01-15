package pl.beone.promena.core.contract.transformation

import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface TransformationService {

    /**
     * Executes [transformation] using [dataDescriptor].
     *
     * @return a descriptor of the transformed data
     * @throws TransformationException if the transformation isn't supported or an error has occurred during the execution of the transformation
     * @throws TransformationTerminationException if the transformation execution has been abruptly terminated
     */
    fun transform(transformation: Transformation, dataDescriptor: DataDescriptor): TransformedDataDescriptor
}