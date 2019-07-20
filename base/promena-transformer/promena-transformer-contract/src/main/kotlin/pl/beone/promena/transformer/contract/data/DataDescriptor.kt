package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

sealed class DataDescriptor {

    object Empty : DataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    data class Single internal constructor(val data: Data,
                                           val mediaType: MediaType,
                                           val metadata: Metadata) : DataDescriptor() {

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(override val descriptors: List<Single>) : DataDescriptor()

    abstract val descriptors: List<Single>
}