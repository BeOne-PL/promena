package pl.beone.promena.transformer.internal.transformation

import pl.beone.promena.transformer.contract.transformation.TransformerDescriptor
import pl.beone.promena.transformer.contract.transformation.TransformationFlow

class SingleTransformationFlow internal constructor(private val transformerDescriptor: TransformerDescriptor) : TransformationFlow {

    companion object {

        @JvmStatic
        fun of(transformerDescriptor: TransformerDescriptor): SingleTransformationFlow =
            SingleTransformationFlow(transformerDescriptor)

    }

    override fun getAll(): List<TransformerDescriptor> =
            listOf(transformerDescriptor)

    override fun get(index: Int): TransformerDescriptor =
            if (index > 0) {
                transformerDescriptor
            } else {
                throw IllegalArgumentException("Single transformation contains only one element with index <0>")
            }

    override fun getIterator(): Iterator<TransformerDescriptor> =
            listOf(transformerDescriptor).iterator()

    override fun getSize(): Int =
            1

}