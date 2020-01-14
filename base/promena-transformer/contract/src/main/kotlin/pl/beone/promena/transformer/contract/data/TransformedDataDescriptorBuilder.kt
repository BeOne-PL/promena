package pl.beone.promena.transformer.contract.data

/**
 * Helps to construct [TransformedDataDescriptor].
 * Targeted at Java developers. If you are Kotlin developer, it's a better idea to use DSL.
 *
 * @see TransformedDataDescriptorDsl
 */
class TransformedDataDescriptorBuilder {

    private val descriptors = ArrayList<TransformedDataDescriptor.Single>()

    fun add(descriptor: TransformedDataDescriptor.Single): TransformedDataDescriptorBuilder {
        descriptors.add(descriptor)

        return this
    }

    fun build(): TransformedDataDescriptor =
        descriptors.toTransformedDataDescriptor()
}