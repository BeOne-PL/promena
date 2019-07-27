package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

sealed class DataDescriptor {

    object Empty : DataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    data class Single private constructor(
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

    data class Multi private constructor(
        override val descriptors: List<Single>
    ) : DataDescriptor() {

        companion object {
            @JvmStatic
            fun of(descriptors: List<Single>): Multi =
                Multi(descriptors)
        }

    }

    abstract val descriptors: List<Single>
}