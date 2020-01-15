package pl.beone.promena.core.contract.transformation

import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface TransformationUseCase {

    /**
     * Executes [transformation] using [dataDescriptor].
     * In additional to [TransformationService], it deals with *external communication* and *internal communication*.
     *
     * @return a descriptor of the transformed data considering [externalCommunicationParameters]
     * @throws TransformationException if the transformation isn't supported or an error has occurred during the execution of the transformation
     * @throws TransformationTerminationException if the transformation execution has been abruptly terminated
     *
     * @see [TransformationService.transform]
     */
    fun transform(
        transformation: Transformation,
        dataDescriptor: DataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor
}