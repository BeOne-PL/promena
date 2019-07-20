package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.transformation.Transformation

data class TransformationDescriptor internal constructor(val transformation: Transformation,
                                                         val dataDescriptors: DataDescriptors) {

    companion object {

        @JvmStatic
        fun of(transformation: Transformation, dataDescriptors: DataDescriptors): TransformationDescriptor =
                TransformationDescriptor(transformation, dataDescriptors)

    }

}