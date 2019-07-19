package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

data class SequentialTransformationFlow internal constructor(private val transformerDescriptors: List<TransformerDescriptor>) : TransformationFlow {

    companion object {

        @JvmStatic
        fun of(transformerDescriptor: TransformerDescriptor): SequentialTransformationFlow =
            SequentialTransformationFlow(listOf(transformerDescriptor))

        @JvmStatic
        fun of(transformerDescriptors: List<TransformerDescriptor>): SequentialTransformationFlow =
            SequentialTransformationFlow(transformerDescriptors)

        @JvmStatic
        fun beginFlow(): SequentialTransformationFlowBuilder =
                SequentialTransformationFlowBuilder()

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