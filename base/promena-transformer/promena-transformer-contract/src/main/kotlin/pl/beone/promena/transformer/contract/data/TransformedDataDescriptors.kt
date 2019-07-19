package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

sealed class TransformedDataDescriptors {

    object Empty : TransformedDataDescriptors() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

    data class Single internal constructor(val data: Data,
                                           val metadata: Metadata) : TransformedDataDescriptors() {

        override val descriptors: List<Single>
            get() = listOf(this)
    }

    data class Multi internal constructor(override val descriptors: List<Single>) : TransformedDataDescriptors() {

        companion object {

            @JvmStatic
            fun of(descriptors: List<Single>): Multi =
                    Multi(descriptors)
        }
    }

    abstract val descriptors: List<Single>

}