package pl.beone.promena.transformer.contract.data

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.model.Metadata

data class DataDescriptor internal constructor(override val data: Data,
                                              val mediaType: MediaType,
                                              val metadata: Metadata) : AbstractDataDescriptor(data) {

    companion object {

        @JvmStatic
        fun of(data: Data, mediaType: MediaType, metadata: Metadata): DataDescriptor =
                DataDescriptor(data, mediaType, metadata)

    }

}