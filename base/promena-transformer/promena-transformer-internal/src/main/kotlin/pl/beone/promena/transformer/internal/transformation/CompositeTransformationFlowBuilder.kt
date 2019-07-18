package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor

data class CompositeTransformationFlowBuilder internal constructor(private val transformerDescriptors: MutableList<TransformerDescriptor>) {

    fun then(transformerDescriptor: TransformerDescriptor): CompositeTransformationFlowBuilder =
            apply { transformerDescriptors.add(transformerDescriptor) }

    fun then(id: String, targetMediaType: MediaType, parameters: Parameters): CompositeTransformationFlowBuilder =
            apply { transformerDescriptors.add(TransformerDescriptor.of(id, targetMediaType, parameters)) }

    fun build(): CompositeTransformationFlowBuilder =
            if (transformerDescriptors.isNotEmpty()) {
                CompositeTransformationFlowBuilder(transformerDescriptors)
            } else
                throw IllegalArgumentException("You have to build composite transformation from at least one transformer")
}
