package pl.beone.promena.transformer.internal.data

import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.DataDescriptors

data class SequentialDataDescriptors internal constructor(private val dataDescriptors: List<DataDescriptor>) : DataDescriptors {

    companion object {

        @JvmStatic
        fun of(dataDescriptor: DataDescriptor): SequentialDataDescriptors =
                SequentialDataDescriptors(listOf(dataDescriptor))

        @JvmStatic
        fun of(dataDescriptors: List<DataDescriptor>): SequentialDataDescriptors =
                SequentialDataDescriptors(dataDescriptors)

        @JvmStatic
        fun builder(): SequentialDataDescriptorsBuilder =
                SequentialDataDescriptorsBuilder()

    }

    override fun getAll(): List<DataDescriptor> =
            dataDescriptors

    override fun get(index: Int): DataDescriptor =
            dataDescriptors[index]

    override fun getIterator(): Iterator<DataDescriptor> =
            dataDescriptors.iterator()

    override fun getSize(): Int =
            dataDescriptors.size

}