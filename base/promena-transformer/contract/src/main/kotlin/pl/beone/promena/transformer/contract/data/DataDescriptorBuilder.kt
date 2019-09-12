package pl.beone.promena.transformer.contract.data

class DataDescriptorBuilder {

    private val descriptors = ArrayList<DataDescriptor.Single>()

    fun add(descriptor: DataDescriptor.Single): DataDescriptorBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): DataDescriptor =
        descriptors.toDataDescriptor()
}