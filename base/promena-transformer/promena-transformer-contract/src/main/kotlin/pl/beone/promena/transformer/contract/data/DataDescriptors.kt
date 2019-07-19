package pl.beone.promena.transformer.contract.data

interface DataDescriptors {

    fun getAll(): List<DataDescriptor>

    fun get(index: Int): DataDescriptor

    fun getIterator(): Iterator<DataDescriptor>

    fun getSize(): Int

}