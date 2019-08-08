@file:JvmName("PerformedTransformationDescriptorDsl")

package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

fun performedTransformationDescriptor(
    transformation: Transformation,
    transformedDataDescriptor: TransformedDataDescriptor
): PerformedTransformationDescriptor =
    PerformedTransformationDescriptor.of(transformation, transformedDataDescriptor)