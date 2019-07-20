package pl.beone.promena.transformer.contract.data

class TransformedDataDescriptorsBuilder {

    private val descriptors = ArrayList<TransformedDataDescriptors.Single>()

    fun add(descriptor: TransformedDataDescriptors.Single): TransformedDataDescriptorsBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): TransformedDataDescriptors =
            descriptors.toTransformedDataDescriptors()
}