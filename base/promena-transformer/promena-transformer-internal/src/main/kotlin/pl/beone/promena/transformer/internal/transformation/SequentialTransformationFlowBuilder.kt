package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.TransformationFlow
import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor

data class SequentialTransformationFlowBuilder internal constructor(private val transformerDescriptors: MutableList<TransformerDescriptor> = ArrayList()) {

    fun next(transformerDescriptor: TransformerDescriptor): SequentialTransformationFlowBuilder =
            apply { transformerDescriptors.add(transformerDescriptor) }

    fun next(id: String, targetMediaType: MediaType, parameters: Parameters): SequentialTransformationFlowBuilder =
            apply { transformerDescriptors.add(TransformerDescriptor.of(id, targetMediaType, parameters)) }

    fun end(): TransformationFlow =
            when (transformerDescriptors.size) {
                0    -> throw IllegalArgumentException("You have to pass at least one transformer")
                else -> SequentialTransformationFlow(transformerDescriptors)
            }
}
