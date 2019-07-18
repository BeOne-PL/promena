package pl.beone.promena.transformer.contract.transformation

interface TransformationFlow {

    fun getAll(): List<TransformerDescriptor>

    fun get(index: Int): TransformerDescriptor

    fun getIterator(): Iterator<TransformerDescriptor>

    fun getSize(): Int

}