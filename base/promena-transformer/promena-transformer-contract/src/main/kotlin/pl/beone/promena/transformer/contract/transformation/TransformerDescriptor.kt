package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

data class TransformerDescriptor internal constructor(val id: String,
                                                      val targetMediaType: MediaType,
                                                      val parameters: Parameters) {

    companion object {

        @JvmStatic
        fun of(id: String, targetMediaType: MediaType, parameters: Parameters): TransformerDescriptor =
                TransformerDescriptor(id, targetMediaType, parameters)

    }
}