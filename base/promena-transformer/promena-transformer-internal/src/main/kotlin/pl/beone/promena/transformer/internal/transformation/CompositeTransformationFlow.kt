package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

class CompositeTransformationFlow internal constructor(private val transformerDescriptors: List<TransformerDescriptor>) : TransformationFlow {

    companion object {

        @JvmStatic
        fun of(transformerDescriptors: List<TransformerDescriptor>): CompositeTransformationFlow =
            CompositeTransformationFlow(transformerDescriptors)

        @JvmStatic
        fun builder(): CompositeTransformationFlowBuilder =
                CompositeTransformationFlowBuilder(ArrayList(listOf()))

    }

    override fun getAll(): List<TransformerDescriptor> =
            transformerDescriptors

    override fun get(index: Int): TransformerDescriptor =
            transformerDescriptors[index]

    override fun getIterator(): Iterator<TransformerDescriptor> =
            transformerDescriptors.iterator()

    override fun getSize(): Int =
            transformerDescriptors.size

}