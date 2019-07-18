package pl.beone.promena.core.applicationmodel.transformation

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

data class TransformationDescriptor(val transformationFlow: TransformationFlow,
                                    val dataDescriptors: List<DataDescriptor>)