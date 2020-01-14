package pl.beone.promena.transformer.contract.data

/**
 * Helps to construct [DataDescriptor].
 * Targeted at Java developers. If you are Kotlin developer, it's a better idea to use DSL.
 *
 * @see DataDescriptorDsl
 */
class DataDescriptorBuilder {

    private val descriptors = ArrayList<DataDescriptor.Single>()

    fun add(descriptor: DataDescriptor.Single): DataDescriptorBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): DataDescriptor =
        descriptors.toDataDescriptor()
}