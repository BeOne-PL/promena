package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class TransformedDataDescriptor(override val data: Data,
                                     val metadata: Metadata) : AbstractDataDescriptor(data)