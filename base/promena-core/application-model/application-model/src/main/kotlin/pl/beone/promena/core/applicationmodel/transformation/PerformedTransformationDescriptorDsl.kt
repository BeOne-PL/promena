@file:JvmName("PerformedTransformationDescriptorDsl")

package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor

fun performedTransformationDescriptor(
    transformedDataDescriptor: TransformedDataDescriptor
): PerformedTransformationDescriptor =
    PerformedTransformationDescriptor.of(transformedDataDescriptor)