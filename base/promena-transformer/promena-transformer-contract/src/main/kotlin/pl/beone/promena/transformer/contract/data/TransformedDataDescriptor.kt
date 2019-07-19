package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class TransformedDataDescriptor internal constructor(override val data: Data,
                                                          val metadata: Metadata) : AbstractDataDescriptor(data) {

    companion object {

        fun of(data: Data, metadata: Metadata): TransformedDataDescriptor =
                TransformedDataDescriptor(data, metadata)

    }
}