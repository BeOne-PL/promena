package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

/**
 * Provides a full description of data used by transformers to perform a transformation.
 * A data descriptor can consist of many [DataDescriptor.Single]. Many [DataDescriptor.Single] make up [DataDescriptor.Multi].
 * [DataDescriptor.Empty] represents a data descriptor without any information.
 *
 * @see DataDescriptorDsl
 * @see DataDescriptorBuilder
 */
sealed class DataDescriptor {

    object Empty : DataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    /**
     * @param mediaType the media type of the data
     * @param metadata the metadata of the data
     */
    data class Single internal constructor(
        val data: Data,
        val mediaType: MediaType,
        val metadata: Metadata
    ) : DataDescriptor() {

        companion object {
            @JvmStatic
            fun of(data: Data, mediaType: MediaType, metadata: Metadata): Single =
                Single(data, mediaType, metadata)
        }

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(
        override val descriptors: List<Single>
    ) : DataDescriptor() {

        companion object {
            @JvmStatic
            fun of(descriptors: List<Single>): Multi =
                Multi(descriptors)
        }

    }

    /**
     * The list of [DataDescriptor.Single] making up the whole data descriptor.
     */
    abstract val descriptors: List<Single>
}