package pl.beone.promena.core.contract.transformation

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface TransformationService {

    fun transform(transformation: Transformation, dataDescriptor: DataDescriptor): TransformedDataDescriptor
}