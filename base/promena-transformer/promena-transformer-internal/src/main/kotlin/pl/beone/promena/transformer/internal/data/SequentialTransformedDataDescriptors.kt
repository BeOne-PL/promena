package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors

data class SequentialTransformedDataDescriptors internal constructor(private val transformedDataDescriptor: List<TransformedDataDescriptor>) : TransformedDataDescriptors {

    companion object {

        @JvmStatic
        fun of(transformedDataDescriptor: TransformedDataDescriptor): SequentialTransformedDataDescriptors =
                SequentialTransformedDataDescriptors(listOf(transformedDataDescriptor))

        @JvmStatic
        fun of(transformedDataDescriptors: List<TransformedDataDescriptor>): SequentialTransformedDataDescriptors =
                SequentialTransformedDataDescriptors(transformedDataDescriptors)

        @JvmStatic
        fun builder(): SequentialTransformedDataDescriptorsBuilder =
                SequentialTransformedDataDescriptorsBuilder()

    }

    override fun getAll(): List<TransformedDataDescriptor> =
            transformedDataDescriptor

    override fun get(index: Int): TransformedDataDescriptor =
            transformedDataDescriptor[index]

    override fun getIterator(): Iterator<TransformedDataDescriptor> =
            transformedDataDescriptor.iterator()

    override fun getSize(): Int =
            transformedDataDescriptor.size

}