package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

data class TransformationDescriptor internal constructor(val transformationFlow: TransformationFlow,
                                                         val dataDescriptors: DataDescriptors) {

    companion object {

        @JvmStatic
        fun of(transformationFlow: TransformationFlow, dataDescriptors: DataDescriptors): TransformationDescriptor =
                TransformationDescriptor(transformationFlow, dataDescriptors)

    }

}