package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

/**
 * Provides a full description of data transformed by transformers.
 * It is very similar to [DataDescriptor]. The only difference is that a transformed data descriptor doesn't contain information about media type.
 * Media type depends on a transformation in which a data descriptor participated ([Transformation.Single.targetMediaType][pl.beone.promena.transformer.contract.transformation.Transformation.Single.targetMediaType]).
 *
 * @see TransformedDataDescriptorDsl
 * @see TransformedDataDescriptorBuilder
 */
sealed class TransformedDataDescriptor {

    object Empty : TransformedDataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    /**
     * @param metadata the metadata of the data
     */
    data class Single internal constructor(
        val data: Data,
        val metadata: Metadata
    ) : TransformedDataDescriptor() {

        companion object {
            @JvmStatic
            fun of(data: Data, metadata: Metadata): Single =
                Single(data, metadata)
        }

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(override val descriptors: List<Single>) : TransformedDataDescriptor() {

        companion object {
            @JvmStatic
            fun of(descriptors: List<Single>): Multi =
                Multi(descriptors)
        }
    }

    /**
     * The list of [TransformedDataDescriptor.Single] making up the whole transformed data descriptor.
     */
    abstract val descriptors: List<Single>
}