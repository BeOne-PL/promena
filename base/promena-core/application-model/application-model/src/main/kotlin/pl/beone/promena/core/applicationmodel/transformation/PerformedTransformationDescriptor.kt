package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

data class PerformedTransformationDescriptor internal constructor(
    val transformedDataDescriptor: TransformedDataDescriptor
) {

    companion object {
        @JvmStatic
        fun of(transformedDataDescriptor: TransformedDataDescriptor): PerformedTransformationDescriptor =
            PerformedTransformationDescriptor(transformedDataDescriptor)
    }
}