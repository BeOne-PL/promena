package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

data class TransformationDescriptor private constructor(
    val transformation: Transformation,
    val dataDescriptor: DataDescriptor
) {

    companion object {
        @JvmStatic
        fun of(transformation: Transformation, dataDescriptor: DataDescriptor): TransformationDescriptor =
            TransformationDescriptor(transformation, dataDescriptor)
    }
}