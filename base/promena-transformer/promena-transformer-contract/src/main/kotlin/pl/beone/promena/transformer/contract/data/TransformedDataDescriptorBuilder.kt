package pl.beone.promena.transformer.contract.data

class TransformedDataDescriptorBuilder {

    private val descriptors = ArrayList<TransformedDataDescriptor.Single>()

    fun add(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptorBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): TransformedDataDescriptor =
            descriptors.toTransformedDataDescriptor()
}