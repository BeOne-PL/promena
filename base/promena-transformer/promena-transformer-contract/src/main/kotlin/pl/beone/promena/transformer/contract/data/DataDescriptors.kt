package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

sealed class DataDescriptors {

    object Empty : DataDescriptors() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    data class Single internal constructor(val data: Data,
                                           val mediaType: MediaType,
                                           val metadata: Metadata) : DataDescriptors() {

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(override val descriptors: List<Single>) : DataDescriptors() {

        companion object {

            @JvmStatic
            fun of(descriptors: List<Single>): Multi =
                    Multi(descriptors)
        }
    }

    abstract val descriptors: List<Single>
}