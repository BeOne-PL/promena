package pl.beone.promena.transformer.contract.data

interface TransformedDataDescriptors {

    fun getAll(): List<TransformedDataDescriptor>

    fun get(index: Int): TransformedDataDescriptor

    fun getIterator(): Iterator<TransformedDataDescriptor>

    fun getSize(): Int

}