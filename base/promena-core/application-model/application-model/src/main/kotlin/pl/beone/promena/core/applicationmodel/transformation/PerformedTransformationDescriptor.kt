package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

data class PerformedTransformationDescriptor internal constructor(
    val transformation: Transformation,
    val transformedDataDescriptor: TransformedDataDescriptor
) {

    companion object {
        @JvmStatic
        fun of(transformation: Transformation, transformedDataDescriptor: TransformedDataDescriptor): PerformedTransformationDescriptor =
            PerformedTransformationDescriptor(transformation, transformedDataDescriptor)
    }
}