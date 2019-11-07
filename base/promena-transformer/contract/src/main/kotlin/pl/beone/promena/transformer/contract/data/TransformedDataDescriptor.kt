package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.model.data.Data

sealed class TransformedDataDescriptor {

    object Empty : TransformedDataDescriptor() {

        override val descriptors: List<Single>
            get() = emptyList()
    }

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

    abstract val descriptors: List<Single>

}