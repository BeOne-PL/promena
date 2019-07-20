package pl.beone.promena.transformer.contract.transformation

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

sealed class Transformation {

    data class Single internal constructor(val id: String,
                                           val targetMediaType: MediaType,
                                           val parameters: Parameters) : Transformation() {

        override val transformers: List<Single>
            get() = listOf(this)
    }

    data class Composite internal constructor(override val transformers: List<Single>) : Transformation() {

        companion object {

            @JvmStatic
            fun of(transformers: List<Single>): Composite =
                    Composite(transformers)
        }
    }

    abstract val transformers: List<Single>

}