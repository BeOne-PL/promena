package pl.beone.promena.transformer.contract.data

class DataDescriptorsBuilder {

    private val descriptors = ArrayList<DataDescriptors.Single>()

    fun add(descriptor: DataDescriptors.Single): DataDescriptorsBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): DataDescriptors =
            dataDescriptors(descriptors)
}