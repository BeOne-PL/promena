package pl.beone.promena.transformer.contract.descriptor

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class DataDescriptor(override val data: Data,
                          val mediaType: MediaType,
                          val metadata: Metadata) : AbstractDescriptor(data)