package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

sealed class TransformedDataDescriptor {

    object Empty : TransformedDataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    data class Single internal constructor(val data: Data,
                                           val metadata: Metadata) : TransformedDataDescriptor() {

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(override val descriptors: List<Single>) : TransformedDataDescriptor()

    abstract val descriptors: List<Single>

}