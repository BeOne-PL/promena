package pl.beone.promena.transformer.contract.descriptor

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data

data class DataDescriptor(override val data: Data,
                          val mediaType: MediaType) : AbstractDescriptor(data)